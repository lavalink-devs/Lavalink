# 🚀 Panduan Deploy Lavalink ke Railway

## Prasyarat
- Akun [Railway.app](https://railway.app) (gratis dengan GitHub)
- Repository ini sudah ter-push ke GitHub
- SSH keys atau PAT untuk GitHub (opsional, Railway bisa auto-connect)

---

## 📋 Langkah-Langkah Deployment

### 1️⃣ Login ke Railway Dashboard

1. Buka https://railway.app/dashboard
2. Klik **"New Project"**

![image](https://user-images.githubusercontent.com/...) 
*(placeholder)*

### 2️⃣ Connect GitHub Repository

1. Pilih **"GitHub Repo"**
2. Authorize Railway untuk akses GitHub Anda
3. Cari dan pilih repository **`Lavalink`**
4. Klik **"Deploy"**

Railway akan otomatis:
- Membaca `Dockerfile` di root repository
- Build Docker image
- Deploy container
- Generate public URL

### 3️⃣ Set Environment Variables

Setelah project terbuat, buka **Settings** → **Variables** dan tambahkan:

| Variable | Nilai | Keterangan |
|----------|-------|-----------|
| `LAVALINK_PASSWORD` | `your-secret-password` | Password untuk authenticate ke server |
| `PORT` | `2333` | Port (Railway auto-assign jika tidak di-set) |

**Contoh:**
```
LAVALINK_PASSWORD=SuperSecurePassword123!
PORT=2333
```

### 4️⃣ Monitoring Deployment

1. Buka **"Deployments"** tab
2. Tunggu sampai status **"Success"** ✅
3. Check logs jika ada error

**Tahapan build:**
- 📦 Build stage (gradle compile)
- 🔨 Create Docker image
- 🚀 Deploy to Railway
- ✅ Service running

---

## 🔗 Akses Server

Setelah deploy berhasil:

1. Buka **"Deployments"** → klik deployment terbaru
2. Copy **Public URL** (contoh: `https://lavalink-prod-123.up.railway.app`)

### Test Connection

```bash
# Check server info
curl -H "Authorization: SuperSecurePassword123!" \
  https://your-railway-url.up.railway.app/info

# Response (jika berhasil):
{
  "version": "4.0.0",
  "buildLine": 1234,
  "git": {...},
  "jvm": "21.0.1"
}
```

### Gunakan di Discord Bot

Konfigurasi client Lavalink Anda:

```javascript
// Contoh: lavalink-client
const nodes = [
  {
    host: "your-railway-url.up.railway.app",
    port: 443,
    password: "SuperSecurePassword123!",
    secure: true // HTTPS
  }
];
```

---

## 📊 Monitoring & Logs

### Check Status
- Dashboard Railway → pilih project → **"Logs"** tab
- Lihat real-time logs dari server

### Common Logs
```
✅ Application ready
✅ Lavalink is ready to accept connections
❌ Failed to bind to port (port already in use)
❌ Authentication failed (password salah)
```

---

## 🔄 Update & Redeploy

Setiap kali Anda push ke GitHub:

1. Railway **otomatis deteksi** perubahan
2. Trigger **automatic rebuild**
3. Test new build
4. **Auto-deploy** jika berhasil

Tidak perlu manual redeploy! 🎉

### Manual Redeploy (jika diperlukan)

1. Railway Dashboard → pilih project
2. Klik menu ⋮ (three dots)
3. Pilih **"Redeploy"** atau **"Cancel Deployment"**

---

## 🐛 Troubleshooting

### ❌ Build gagal: "gradle not found"
**Solusi:**
- Pastikan `build.gradle` ada di root atau subdirectory
- Check Dockerfile path di `railway.toml`

### ❌ Container crash setelah deploy
**Solusi:**
- Check logs: `LAVALINK_PASSWORD` sudah di-set?
- Verify environment variables di Railway dashboard
- Lihat error message di Logs tab

### ❌ Connection timeout / 504 Gateway Timeout
**Solusi:**
- Tunggu 60-120 detik setelah deploy
- Verify password benar
- Check network/firewall tidak memblokir
- Try `curl -v` untuk debug connection

### ❌ Out of memory error
**Solusi:**
- Edit `Dockerfile` line:
  ```dockerfile
  ENTRYPOINT ["java", "-Xmx1g", "-Xms512m", "-jar", "Lavalink.jar"]
  ```
  (Sesuaikan `-Xmx1g` dengan kebutuhan)
- Upgrade Railway plan jika perlu lebih banyak memory

### ❌ Health check failing
**Solusi:**
- Railway akan auto-restart jika health check gagal
- Check server logs untuk error
- Verify port 2333 accessible

---

## 📁 Struktur File

```
Lavalink/
├── Dockerfile              ← Docker configuration
├── railway.toml            ← Railway configuration
├── application.yml         ← Lavalink server config
├── .dockerignore           ← Exclude files dari Docker build
├── LavalinkServer/
│   ├── build.gradle
│   ├── src/
│   └── ...
└── README.md
```

---

## 💰 Railway Pricing

**Free Tier (sufficient untuk Lavalink):**
- $5/month free credits
- Up to 8GB shared memory
- Auto-deploy dari GitHub

**Pro Plan:**
- Unlimited resources
- Custom domains
- Priority support

---

## 🔐 Security Best Practices

1. **Gunakan password yang kuat** untuk `LAVALINK_PASSWORD`
2. **Jangan hardcode password** di repository (gunakan env variables)
3. **Monitor logs** secara berkala
4. **Update Dockerfile** saat ada Java security patch
5. **Backup konfigurasi** di tempat aman

---

## 📞 Resources

- [Railway Documentation](https://docs.railway.app)
- [Lavalink Documentation](https://lavalink.dev)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Railway Troubleshooting](https://docs.railway.app/troubleshooting)

---

## ✨ Tips & Tricks

### Auto-restart jika crash
Railway sudah config di `railway.toml`:
```toml
restartPolicyMaxRetries = 5
restartPolicyWindowSeconds = 600
```

### Monitor resource usage
Railway dashboard menampilkan:
- CPU usage
- Memory usage
- Network I/O
- Uptime

### Custom domain (Pro plan)
Bisa setup domain custom seperti `lavalink.yourdomain.com`

---

## 🎯 Next Steps

1. ✅ Sudah ada `Dockerfile` dan `application.yml`
2. ✅ Sudah ada `railway.toml`
3. 📍 **Anda di sini:** Review konfigurasi
4. 🚀 Push ke GitHub
5. 🌐 Buka Railway → Connect GitHub → Deploy!

**Siap untuk deploy? Mulai dari step 1 di atas!** 🚀

---

**Created:** 2026-07-24  
**Last Updated:** 2026-07-24
