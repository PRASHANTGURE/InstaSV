# InstaSV - Instagram Video, Reel & Picture Downloader

**InstaSV** is a simple, one-click Android app to download videos, reels, and pictures from Instagram without copy-pasting URLs.

## Features

✅ **One-Click Download** - No copy-paste needed  
✅ **Share from Instagram** - Use "Share" directly from Instagram  
✅ **Video & Reel Support** - Download Instagram videos and reels  
✅ **Picture Download** - Save Instagram photos  
✅ **Preview** - See image preview before download  
✅ **Auto Save** - Downloads saved to Downloads folder  
✅ **Fast & Lightweight** - Minimal app size  
✅ **No Ads** - Clean, ad-free experience  

## How to Use

### Method 1: Direct Share (Easiest)
1. Open Instagram
2. Go to the video/reel/picture you want to download
3. Tap the **Share** button (arrow icon)
4. Select **InstaSV**
5. App opens and downloads automatically ✓

### Method 2: Paste Link
1. Copy the Instagram URL (long press on post → Copy Link)
2. Open InstaSV
3. Tap **Paste Link** button
4. Tap **Download**
5. Media saved to Downloads folder ✓

## Installation

### From APK File
1. Download `InstaSV.apk`
2. Go to Settings → Security → Enable "Unknown Sources"
3. Open the APK file and install

### From Android Studio
1. Clone this repository
2. Open in Android Studio
3. Click **Run** → **Run 'app'**

## Building APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (optimized)
./gradlew assembleRelease
```

APK will be in: `app/build/outputs/apk/`

## Requirements

- Android 7.0+ (API 24+)
- Internet connection
- Storage permission

## Permissions Used

- `INTERNET` - To fetch media from Instagram
- `READ_EXTERNAL_STORAGE` - To access clipboard
- `WRITE_EXTERNAL_STORAGE` - To save downloads

## Technical Details

- **Language**: Kotlin
- **Framework**: Android SDK
- **Libraries**: OkHttp, Glide, Coroutines
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Troubleshooting

**Q: App says "Invalid Instagram URL"**  
A: Make sure you're copying the full Instagram post URL (instagram.com/p/...) or reel URL (instagram.com/reel/...)

**Q: Download fails**  
A: Check your internet connection and ensure you have storage space

**Q: Where are my downloads?**  
A: Check Downloads folder on your phone. Path: Files → Downloads

## Disclaimer

This app is for personal use only. Always respect copyright and intellectual property rights. Only download content you have permission to download.

## License

MIT License - Feel free to use and modify

## Support

For issues or suggestions, please open an issue on GitHub.

---

**Made with ❤️ by Prashant Gure**
