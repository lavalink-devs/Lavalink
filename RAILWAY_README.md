# Lavalink Server - Railway Ready ✨

Repositori ini sudah dikonfigurasi untuk deployment di Railway dengan Dockerfile, environment variables, dan monitoring yang optimal.

## 🚀 Quick Start Deploy

### 1. Login ke Railway
Buka https://railway.app/dashboard

### 2. Create New Project
Klik **"New Project"** → Pilih **"GitHub Repo"**

### 3. Connect GitHub
Authorize Railway dan pilih repository ini

### 4. Set Environment Variables
Tambahkan di Railway dashboard:
```
LAVALINK_PASSWORD=your-secure-password
PORT=2333
```

### 5. Deploy!
Railway otomatis build dan deploy

---

## 📋 Files yang Sudah Disiapkan

| File | Purpose |
|------|---------|
| `Dockerfile` | Docker configuration dengan multi-stage build |
| `railway.toml` | Railway deployment configuration |
| `application.yml` | Lavalink server settings |
| `.dockerignore` | Exclude unnecessary files dari build |
| `RAILWAY_GUIDE.md` | Panduan lengkap deployment |

---

## 🔗 Setelah Deploy

Copy public URL dari Railway dashboard dan gunakan di Discord bot:

```javascript
{
  host: "your-railway-url.up.railway.app",
  port: 443,
  password: "your-secure-password",
  secure: true
}
```

---

## 📖 Dokumentasi

- **[RAILWAY_GUIDE.md](RAILWAY_GUIDE.md)** - Panduan lengkap deployment
- **[Lavalink Docs](https://lavalink.dev)** - Official documentation
- **[Railway Docs](https://docs.railway.app)** - Platform documentation

---

## ✅ Deployment Checklist

- [ ] Push repository ke GitHub
- [ ] Buat Railway account di https://railway.app
- [ ] Connect GitHub repository
- [ ] Set `LAVALINK_PASSWORD` environment variable
- [ ] Deploy!

**Status:** ✅ Ready for Railway Deployment

---

**Java Version:** 21 LTS  
**Memory:** 512MB max (configurable)  
**Last Updated:** 2026-07-24
