#!/usr/bin/env python3
"""
AI-Project 统一接口契约测试
===========================
对 backend-java（Spring Boot）与 backend-dotnet（ASP.NET Core）跑同一套断言，
确保双后端行为与 database/API_CONTRACT.md 完全一致。

用法：
    python tests/contract_test.py --base-url http://localhost:8080/api/v1
    python tests/contract_test.py --all          # 对 Java 8080 + .NET 5080 各跑一遍

退出码：0 = 全部通过，1 = 有失败用例
"""
import argparse
import random
import sys
import uuid

import requests

PASS = 0
FAIL = 1
_results: list[tuple[str, bool, str]] = []


def check(name: str, condition: bool, detail: str = "") -> None:
    status = PASS if condition else FAIL
    _results.append((name, condition, detail))
    mark = "✅ PASS" if condition else "❌ FAIL"
    print(f"  {mark}  {name}" + (f"  —— {detail}" if detail else ""))


def unique_suffix() -> str:
    return uuid.uuid4().hex[:8]


def run_suite(base: str) -> int:
    print(f"\n{'=' * 64}\n测试目标: {base}\n{'=' * 64}")
    start_idx = len(_results)
    tag = unique_suffix()
    username = f"tester_{tag}"
    email = f"{username}@example.com"
    password = "passw0rd-123"
    s = requests.Session()

    # ---- 1. 健康检查 ----
    print("\n[1] 健康检查")
    r = s.get(f"{base}/health", timeout=5)
    check("GET /health -> 200 + status=UP",
          r.status_code == 200 and r.json().get("data", {}).get("status") == "UP", f"HTTP {r.status_code}")

    # ---- 2. 注册 ----
    print("\n[2] 注册")
    r = s.post(f"{base}/auth/register", json={"username": username, "email": email, "password": password})
    check("POST /auth/register -> 200 code=0",
          r.status_code == 200 and r.json().get("code") == 0, f"HTTP {r.status_code} {r.text[:120]}")
    check("注册返回 username", (r.json().get("data") or {}).get("username") == username)

    # 重复注册
    r = s.post(f"{base}/auth/register", json={"username": username, "email": email, "password": password})
    check("重复注册 -> 409 + code=40901",
          r.status_code == 409 and r.json().get("code") == 40901, f"HTTP {r.status_code}")

    # 参数校验失败
    r = s.post(f"{base}/auth/register", json={"username": "ab", "email": "bad-email", "password": "123"})
    check("非法参数 -> 400 + code=40001",
          r.status_code == 400 and r.json().get("code") == 40001, f"HTTP {r.status_code}")

    # ---- 3. 登录 ----
    print("\n[3] 登录")
    r = s.post(f"{base}/auth/login", json={"username": username, "password": password})
    body = r.json()
    ok_login = r.status_code == 200 and body.get("code") == 0 and body.get("data", {}).get("token")
    check("POST /auth/login -> 200 + token", ok_login, f"HTTP {r.status_code}")
    token = body.get("data", {}).get("token", "")
    check("tokenType=Bearer", body.get("data", {}).get("tokenType") == "Bearer")
    check("expiresIn=86400", body.get("data", {}).get("expiresIn") == 86400)

    r = s.post(f"{base}/auth/login", json={"username": username, "password": "wrong-pass"})
    check("错误密码 -> 401 + code=40101",
          r.status_code == 401 and r.json().get("code") == 40101, f"HTTP {r.status_code}")

    headers = {"Authorization": f"Bearer {token}"}

    # ---- 4. 当前用户 ----
    print("\n[4] 当前用户 /auth/me")
    r = s.get(f"{base}/auth/me")
    check("me 无 token -> 401", r.status_code == 401, f"HTTP {r.status_code}")
    r = s.get(f"{base}/auth/me", headers=headers)
    check("me 带 token -> 200 + 用户名一致",
          r.status_code == 200 and r.json().get("data", {}).get("username") == username, f"HTTP {r.status_code}")

    # ---- 5. 文章 CRUD ----
    print("\n[5] 文章 CRUD")
    r = s.post(f"{base}/articles", headers=headers,
               json={"title": "契约测试文章", "content": "内容正文", "published": True})
    article = r.json().get("data", {})
    check("POST /articles -> 200 + 创建成功",
          r.status_code == 200 and r.json().get("code") == 0 and article.get("id"), f"HTTP {r.status_code}")
    article_id = article.get("id")
    check("文章作者正确", article.get("author") == username)
    check("文章已发布", article.get("published") is True)

    # 未登录创建 -> 401
    r = s.post(f"{base}/articles", json={"title": "无权限", "content": "x", "published": False})
    check("未登录创建文章 -> 401", r.status_code == 401, f"HTTP {r.status_code}")

    r = s.get(f"{base}/articles?page=1&pageSize=10")
    lst = r.json().get("data", {})
    check("GET /articles -> 列表包含新文章",
          r.status_code == 200 and any(i["id"] == article_id for i in lst.get("items", [])), f"HTTP {r.status_code}")
    check("分页字段齐全", {"total", "page", "pageSize", "items"} <= set(lst.keys()))

    r = s.get(f"{base}/articles/{article_id}")
    detail = r.json().get("data", {})
    check("GET /articles/{id} -> 详情一致",
          r.status_code == 200 and detail.get("title") == "契约测试文章" and detail.get("content") == "内容正文",
          f"HTTP {r.status_code}")

    r = s.put(f"{base}/articles/{article_id}", headers=headers,
              json={"title": "更新后的标题", "content": "更新正文", "published": False})
    check("PUT /articles/{id} -> 更新成功",
          r.status_code == 200 and r.json().get("data", {}).get("title") == "更新后的标题", f"HTTP {r.status_code}")

    # ---- 6. 越权访问（第二个用户） ----
    print("\n[6] 越权访问")
    user2 = f"intruder_{tag}"
    s.post(f"{base}/auth/register", json={"username": user2, "email": f"{user2}@example.com", "password": password})
    r2 = s.post(f"{base}/auth/login", json={"username": user2, "password": password})
    headers2 = {"Authorization": f"Bearer {r2.json().get('data', {}).get('token', '')}"}

    r = s.put(f"{base}/articles/{article_id}", headers=headers2,
              json={"title": "越权", "content": "x", "published": False})
    check("非作者更新 -> 403 + code=40301",
          r.status_code == 403 and r.json().get("code") == 40301, f"HTTP {r.status_code}")
    r = s.delete(f"{base}/articles/{article_id}", headers=headers2)
    check("非作者删除 -> 403", r.status_code == 403, f"HTTP {r.status_code}")

    # ---- 7. 删除 ----
    print("\n[7] 删除")
    r = s.delete(f"{base}/articles/{article_id}", headers=headers)
    check("作者删除 -> 200", r.status_code == 200, f"HTTP {r.status_code}")
    r = s.get(f"{base}/articles/{article_id}")
    check("删除后详情 -> 404 + code=40401",
          r.status_code == 404 and r.json().get("code") == 40401, f"HTTP {r.status_code}")

    # ---- 汇总 ----
    suite_results = _results[start_idx:]
    passed = sum(1 for _, ok, _ in suite_results if ok)
    total = len(suite_results)
    print(f"\n{'=' * 64}\n子套件结果: {passed}/{total} 通过 (当前目标: {base})\n{'=' * 64}")
    return 0 if passed == total else 1


def main() -> int:
    parser = argparse.ArgumentParser(description="AI-Project 双后端契约测试")
    parser.add_argument("--base-url", default=None, help="单个后端地址，如 http://localhost:8080/api/v1")
    parser.add_argument("--all", action="store_true", help="对 Java(8080) 与 .NET(5080) 各跑一遍")
    args = parser.parse_args()

    targets = []
    if args.all:
        targets = ["http://localhost:8080/api/v1", "http://localhost:5080/api/v1"]
    elif args.base_url:
        targets = [args.base_url]
    else:
        print("请指定 --base-url 或 --all", file=sys.stderr)
        return 2

    exit_code = 0
    for t in targets:
        if run_suite(t) != 0:
            exit_code = 1
    return exit_code


if __name__ == "__main__":
    sys.exit(main())
