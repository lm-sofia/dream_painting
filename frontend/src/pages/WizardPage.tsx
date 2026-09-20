import { useEffect, useRef, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { App, Button, Form, Input, Segmented, Space, Steps, Switch, Typography } from 'antd';
import { draftApi, styleApi } from '../api';
import type { DraftPayload, Style } from '../types';
import StyleCard from '../components/StyleCard';

const { TextArea } = Input;

interface WizardState {
  title: string;
  idea: string;
  styleId?: number;
  duration: number;
  ratio: string;
  voiceover: boolean;
}

const DURATION_OPTIONS = [
  { label: '15 秒', value: 15 },
  { label: '30 秒（推荐）', value: 30 },
  { label: '60 秒', value: 60 },
];

const RATIO_OPTIONS = ['9:16', '16:9', '1:1'];

/**
 * 三步向导页（PRD §4.3）：创意 → 风格 → 配置
 * 核心设计：防抖自动保存 —— 用户停止输入 1.5s 后自动落库，不打断创作流
 */
export default function WizardPage() {
  const navigate = useNavigate();
  const { message } = App.useApp();
  const [searchParams] = useSearchParams();
  const draftIdParam = searchParams.get('draftId');

  const [step, setStep] = useState(0);
  const [styles, setStyles] = useState<Style[]>([]);
  const [draftId, setDraftId] = useState<number | null>(null);
  const [form] = Form.useForm<WizardState>();

  // 防抖定时器引用：useRef 跨渲染保存，不清零
  const saveTimer = useRef<number | undefined>(undefined);

  /** 加载风格库（风格选择步骤用） */
  useEffect(() => {
    styleApi.list().then(({ data }) => setStyles(data.data));
  }, []);

  /** 恢复已有草稿（?draftId= 进入续创） */
  useEffect(() => {
    if (draftIdParam) {
      draftApi
        .detail(Number(draftIdParam))
        .then(({ data }) => {
          setDraftId(data.data.id);
          form.setFieldsValue({
            title: data.data.title,
            idea: data.data.idea,
            styleId: data.data.styleId,
            duration: data.data.duration,
            ratio: data.data.ratio,
            voiceover: data.data.voiceover,
          });
        })
        .catch(() => message.error('草稿加载失败'));
    }
  }, [draftIdParam, form, message]);

  /** 保存草稿：有 id → 更新；无 id → 创建并记住 id。返回草稿 id（成功）或 null（失败） */
  const saveDraft = async (): Promise<number | null> => {
    // 按字段名显式取值：antd 5 的 getFieldsValue() 无参只返回“当前挂载”字段，
    // Step3 时 Step0 的 title 字段已卸载，必须点名取（值仍保留在 store）
    const values = form.getFieldsValue([
      'title',
      'idea',
      'styleId',
      'duration',
      'ratio',
      'voiceover',
    ]);
    if (!values.title?.trim()) return null;
    const payload: DraftPayload = {
      title: values.title.trim(),
      idea: values.idea?.trim(),
      styleId: values.styleId,
      duration: values.duration ?? 30,
      ratio: values.ratio ?? '9:16',
      voiceover: values.voiceover ?? true,
    };
    try {
      const { data } = draftId
        ? await draftApi.update(draftId, payload)
        : await draftApi.create(payload);
      setDraftId(data.data.id);
      return data.data.id;
    } catch {
      return null;
    }
  };

  /** 防抖自动保存：表单任何字段变化后 1.5s 保存（教学点：防抖的意义） */
  const debouncedSave = () => {
    if (saveTimer.current) window.clearTimeout(saveTimer.current);
    saveTimer.current = window.setTimeout(() => {
      saveDraft();
    }, 1500);
  };
  /** 下一步：非最后一步先校验必填，最后一步先保存再进生成 */
  const onNext = async () => {
    if (step === 0) {
      const values = form.getFieldsValue();
      if (!values.title?.trim()) {
        message.warning('请先给作品起个标题');
        return;
      }
    }
    if (step === 2) {
      // 用保存返回的 id 跳转，避免闭包读到旧的 draftId（React 状态更新是异步的）
      const savedId = await saveDraft();
      if (savedId != null) {
        message.success('草稿已保存');
        navigate(`/generating?draftId=${savedId}`); // 第 4 课：提交生成任务
      } else {
        message.error('保存失败，请重试');
      }
      return;
    }
    setStep(step + 1);
  };

  return (
    <div style={{ maxWidth: 760, margin: '0 auto' }}>
      <Typography.Title level={3}>开始创作</Typography.Title>
      <Steps
        current={step}
        items={[{ title: '写下创意' }, { title: '选择风格' }, { title: '配置参数' }]}
        style={{ marginBottom: 28 }}
      />

      <Form<WizardState> form={form} layout="vertical" onValuesChange={debouncedSave}>
        {step === 0 && (
          <>
            <Form.Item
              name="title"
              label="作品标题"
              rules={[{ required: true, message: '请输入标题' }]}
            >
              <Input placeholder="例如：会跳舞的猫" size="large" maxLength={100} />
            </Form.Item>
            <Form.Item name="idea" label="一句话创意">
              <TextArea
                placeholder="描述你的脑洞，例如：一只会说话的猫在屋顶上跳街舞，背景是烟花"
                rows={5}
                maxLength={2000}
                showCount
              />
            </Form.Item>
          </>
        )}

        {step === 1 && (
          <Form.Item
            name="styleId"
            label="选择风格"
            rules={[{ required: true, message: '请选择风格' }]}
          >
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(170px, 1fr))',
                gap: 14,
              }}
            >
              {styles.map((style) => (
                <Form.Item key={style.id} noStyle shouldUpdate>
                  {({ getFieldValue, setFieldValue }) => (
                    <StyleCard
                      style={style}
                      selected={getFieldValue('styleId') === style.id}
                      onClick={() => {
                        setFieldValue('styleId', style.id);
                        debouncedSave();
                      }}
                    />
                  )}
                </Form.Item>
              ))}
            </div>
          </Form.Item>
        )}

        {step === 2 && (
          <>
            <Form.Item name="duration" label="视频时长">
              <Segmented options={DURATION_OPTIONS} block />
            </Form.Item>
            <Form.Item name="ratio" label="画面比例">
              <Segmented
                options={RATIO_OPTIONS.map((r) => ({
                  label:
                    r === '9:16' ? '竖屏 9:16（抖音）' : r === '16:9' ? '横屏 16:9' : '方形 1:1',
                  value: r,
                }))}
                block
              />
            </Form.Item>
            <Form.Item name="voiceover" label="AI 配音与音效" valuePropName="checked">
              <Switch checkedChildren="开启" unCheckedChildren="关闭" />
            </Form.Item>
            <Typography.Paragraph type="secondary" style={{ fontSize: 12 }}>
              预计消耗：1 次创作额度（免费用户每月 10 次）
            </Typography.Paragraph>
          </>
        )}
      </Form>

      <Space style={{ marginTop: 8 }}>
        {step > 0 && <Button onClick={() => setStep(step - 1)}>上一步</Button>}
        <Button type="primary" onClick={onNext}>
          {step === 2 ? '保存并开始生成' : '下一步'}
        </Button>
      </Space>
    </div>
  );
}
