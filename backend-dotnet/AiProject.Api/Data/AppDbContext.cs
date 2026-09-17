using Microsoft.EntityFrameworkCore;

namespace AiProject.Api.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

    public DbSet<Models.User> Users => Set<Models.User>();
    public DbSet<Models.Article> Articles => Set<Models.Article>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<Models.User>(entity =>
        {
            entity.HasIndex(u => u.Username).IsUnique();
            entity.HasIndex(u => u.Email).IsUnique();
        });

        modelBuilder.Entity<Models.Article>(entity =>
        {
            entity.HasIndex(a => a.UserId);
            entity.HasIndex(a => new { a.Published, a.CreatedAt });
            entity.HasOne<Models.User>()
                .WithMany()
                .HasForeignKey(a => a.UserId)
                .OnDelete(DeleteBehavior.Cascade);
        });
    }
}
