package vn.daopham.codcleaner

/** Mức độ an toàn khi gỡ. */
enum class Risk(val label: String) {
    SAFE("An toàn"),
    CAUTION("Cẩn thận"),
    CRITICAL("Không được gỡ"),
    UNKNOWN("Chưa rõ"),
    USER("Đã cài")
}

data class Rule(val risk: Risk, val name: String, val note: String)

/**
 * Danh sách phân loại cho Xiaomi HyperOS bản nội địa (ROM Trung Quốc).
 * Gỡ ở đây là "pm uninstall --user 0": không xoá khỏi hệ thống, khôi phục lại được.
 */
object Catalog {

    private fun s(name: String, note: String) = Rule(Risk.SAFE, name, note)
    private fun c(name: String, note: String) = Rule(Risk.CAUTION, name, note)
    private fun x(name: String, note: String) = Rule(Risk.CRITICAL, name, note)

    private val map: Map<String, Rule> = mapOf(
        // ===== Quảng cáo, theo dõi =====
        "com.miui.systemAdSolution" to s("MSA – Quảng cáo hệ thống", "Dịch vụ đẩy quảng cáo của Xiaomi. Gỡ để bớt quảng cáo."),
        "com.miui.msa.global" to s("MSA – Quảng cáo", "Dịch vụ quảng cáo. Gỡ để bớt quảng cáo."),
        "com.miui.analytics" to s("Analytics", "Thu thập dữ liệu sử dụng gửi về Xiaomi."),
        "com.miui.daemon" to s("MIUI Daemon", "Thu thập thông số hệ thống để gửi báo cáo."),
        "com.miui.uireporter" to s("UI Reporter", "Báo cáo giao diện, không cần thiết."),
        "com.miui.bugreport" to s("Phản hồi lỗi", "Gửi báo lỗi cho Xiaomi."),
        "com.miui.klo.bugreport" to s("KLO Bug Report", "Báo lỗi nhân hệ thống."),
        "com.miui.audiomonitor" to s("Audio Monitor", "Theo dõi âm thanh để thống kê."),
        "com.xiaomi.ab" to s("Xiaomi AB", "Dịch vụ thử nghiệm A/B tính năng."),
        "com.miui.hybrid" to s("Quick Apps", "Ứng dụng nhanh kiểu Trung Quốc, hay bật quảng cáo."),
        "com.miui.hybrid.accessory" to s("Quick Apps phụ trợ", "Đi kèm Quick Apps."),
        "com.miui.contentextension" to s("Taplus", "Nhấn giữ để tra cứu, toàn nội dung Trung Quốc."),
        "com.miui.newhome" to s("Bảng tin (Feed)", "Tin tức Trung Quốc ở màn hình chính."),
        "com.miui.personalassistant" to c("App Vault", "Trang widget bên trái màn hình chính. Gỡ thì mất trang này."),
        "com.mfashiongallery.emag" to s("Hình nền tự đổi", "Màn hình khoá hình nền + quảng cáo."),
        "com.miui.android.fashiongallery" to s("Hình nền tự đổi", "Màn hình khoá hình nền + quảng cáo."),

        // ===== Trợ lý ảo XiaoAi (tiếng Trung) =====
        "com.miui.voiceassist" to s("Trợ lý XiaoAi", "Trợ lý giọng nói tiếng Trung, không dùng được tiếng Việt."),
        "com.miui.voicetrigger" to s("Kích hoạt giọng nói", "Đánh thức XiaoAi bằng giọng nói."),
        "com.xiaomi.mibrain.speech" to s("XiaoAi Speech", "Nhận dạng giọng nói tiếng Trung."),
        "com.xiaomi.aiasst.service" to s("XiaoAi nghe máy hộ", "Trợ lý trả lời cuộc gọi bằng tiếng Trung."),
        "com.xiaomi.aiasst.vision" to s("Phụ đề AI", "Phụ đề trực tiếp tiếng Trung."),
        "com.xiaomi.aireco" to c("Gợi ý thông minh", "Gợi ý theo ngữ cảnh. Có thể ảnh hưởng thẻ gợi ý."),
        "com.xiaomi.aicr" to c("AI Engine", "Dịch vụ AI nền của HyperOS. Nên giữ nếu dùng tính năng AI."),
        "com.miui.translationservice" to s("Dịch vụ dịch", "Dịch thuật Trung–Anh."),
        "com.miui.translation.kingsoft" to s("Dịch Kingsoft", "Từ điển Kingsoft."),
        "com.miui.translation.youdao" to s("Dịch Youdao", "Từ điển Youdao."),
        "com.miui.translation.xmcloud" to s("Dịch Xiaomi Cloud", "Dịch thuật qua mây."),

        // ===== Ứng dụng Xiaomi Trung Quốc =====
        "com.xiaomi.shop" to s("Mi Store", "Cửa hàng Xiaomi Trung Quốc."),
        "com.xiaomi.youpin" to s("Youpin", "Sàn hàng Xiaomi Youpin."),
        "com.xiaomi.jr" to s("Mi Finance", "Tài chính, vay tiêu dùng Trung Quốc."),
        "com.mipay.wallet" to s("Mi Pay", "Ví Mi Pay (Trung Quốc)."),
        "com.miui.nextpay" to s("NextPay", "Thanh toán Xiaomi."),
        "com.miui.tsmclient" to s("Thẻ NFC (Mi Pay)", "Thẻ giao thông Trung Quốc."),
        "com.unionpay.tsmservice.mi" to s("UnionPay", "Thanh toán UnionPay."),
        "com.xiaomi.payment" to c("Thanh toán Xiaomi", "Dùng để mua giao diện/ứng dụng trong hệ sinh thái Xiaomi."),
        "com.miui.virtualsim" to s("SIM ảo Xiaomi", "Dịch vụ SIM du lịch Trung Quốc."),
        "com.miui.vsimcore" to s("SIM ảo – lõi", "Đi kèm SIM ảo."),
        "com.xiaomi.mirecycle" to s("Thu cũ đổi mới", "Dịch vụ thu mua máy cũ."),
        "com.xiaomi.gamecenter" to s("Game Center", "Kho game Trung Quốc."),
        "com.xiaomi.gamecenter.sdk.service" to c("Game SDK", "Đăng nhập game bản Trung Quốc. Gỡ nếu không chơi game TQ."),
        "com.xiaomi.migameservice" to s("Game Service", "Dịch vụ game Xiaomi."),
        "com.miui.player" to s("Mi Music", "Nghe nhạc Xiaomi."),
        "com.miui.video" to s("Mi Video", "Xem video Xiaomi, nhiều quảng cáo."),
        "com.miui.fm" to s("Radio FM", "Đài FM."),
        "com.android.browser" to s("Trình duyệt Xiaomi", "Trình duyệt mặc định TQ, nhiều quảng cáo. Nhớ cài Chrome trước."),
        "com.miui.yellowpage" to s("Trang vàng", "Danh bạ doanh nghiệp Trung Quốc."),
        "com.miui.huanji" to s("Mi Mover", "Chuyển dữ liệu máy cũ sang máy mới."),
        "com.duokan.phone.remotecontroller" to s("Mi Remote", "Điều khiển hồng ngoại."),
        "com.miui.compass" to s("La bàn", "La bàn."),
        "com.android.email" to s("Email Xiaomi", "Ứng dụng email, dùng Gmail thì gỡ được."),
        "com.xiaomi.smarthome" to s("Mi Home", "Điều khiển thiết bị nhà thông minh (bản TQ)."),
        "com.miui.carlink" to s("Kết nối ô tô", "Carlink cho xe Trung Quốc."),
        "com.miui.greenguard" to s("Bảo vệ gia đình", "Quản lý trẻ em bản TQ."),
        "com.miui.thirdappassistant" to s("Trợ lý ứng dụng", "Thông báo lỗi ứng dụng bên thứ ba."),
        "com.miui.miservice" to s("Dịch vụ & Phản hồi", "Hỗ trợ khách hàng Xiaomi TQ."),
        "com.miui.touchassistant" to s("Quả cầu nổi", "Quick Ball."),
        "com.miui.phrase" to s("Cụm từ thường dùng", "Câu trả lời nhanh trên bàn phím."),
        "com.xiaomi.barrage" to s("Bong bóng tin nhắn", "Thông báo nổi khi chơi game."),
        "com.miui.cleanmaster" to c("Dọn dẹp Xiaomi", "Trình dọn rác của Xiaomi, đi kèm Bảo mật."),
        "com.miui.cleaner" to c("Dọn dẹp Xiaomi", "Trình dọn rác của Xiaomi, đi kèm Bảo mật."),
        "com.miui.notes" to c("Ghi chú", "Có ghi chú của anh thì đừng gỡ."),
        "com.miui.weather2" to c("Thời tiết", "Gỡ thì mất widget thời tiết."),
        "com.miui.calculator" to c("Máy tính", "Máy tính bỏ túi."),
        "com.miui.screenrecorder" to c("Quay màn hình", "Quay màn hình."),
        "com.xiaomi.scanner" to c("Quét mã", "Quét QR/tài liệu."),
        "com.miui.mediaeditor" to x("Sửa ảnh/video (Gallery Editor)", "Cần cho chụp màn hình cuộn, sửa ảnh chụp màn hình và chỉnh sửa trong Thư viện."),
        "com.miui.cit" to c("CIT – kiểm tra phần cứng", "Menu kiểm tra phần cứng."),
        "com.xiaomi.joyose" to c("Joyose", "Quản lý hiệu năng khi chơi game. Gỡ có thể làm máy nóng/giật hơn."),
        "com.miui.freeform" to c("Cửa sổ nổi", "Mở ứng dụng dạng cửa sổ nhỏ."),
        "com.xiaomi.vipaccount" to c("Cộng đồng Xiaomi", "CẦN app này nếu sau này muốn mở khoá bootloader."),
        "com.xiaomi.market" to c("GetApps (kho ứng dụng)", "Kho ứng dụng Xiaomi, cũng cập nhật app hệ thống."),
        "com.xiaomi.discover" to c("Cập nhật ứng dụng hệ thống", "Cập nhật app hệ thống."),
        "com.miui.cloudservice" to c("Xiaomi Cloud", "Đồng bộ đám mây. Cần cho Tìm thiết bị."),
        "com.miui.cloudbackup" to c("Sao lưu đám mây", "Sao lưu lên Xiaomi Cloud."),
        "com.miui.micloudsync" to c("Đồng bộ Cloud", "Đồng bộ danh bạ/ghi chú với Xiaomi Cloud."),
        "com.miui.backup" to c("Sao lưu cục bộ", "Sao lưu trong máy."),
        "com.android.thememanager" to c("Giao diện (Themes)", "Gỡ có thể lỗi đổi hình nền/giao diện."),
        "com.miui.mishare.connectivity" to c("Mi Share", "Chia sẻ file giữa máy Xiaomi."),
        "com.xiaomi.mi_connect_service" to c("Mi Connect", "Kết nối thiết bị Xiaomi."),
        "com.milink.service" to c("Truyền màn hình", "Cast màn hình lên TV."),
        "com.miui.accessibility" to c("Trợ năng Xiaomi", "Tính năng trợ năng."),
        "com.xiaomi.simactivate.service" to c("Kích hoạt SIM", "Kích hoạt tài khoản qua SIM."),
        "com.miui.gallery" to x("Thư viện ảnh", "Cần cho xem/sửa ảnh chụp màn hình và chụp màn hình cuộn."),
        "com.miui.screenshot" to x("Chụp màn hình", "Chụp màn hình và chụp cuộn."),

        // ===== Bàn phím Trung Quốc (tự bảo vệ nếu đang dùng) =====
        "com.baidu.input_mi" to c("Bàn phím Baidu", "Chỉ gỡ khi đã chuyển sang Gboard/Laban Key."),
        "com.sohu.inputmethod.sogou.xiaomi" to c("Bàn phím Sogou", "Chỉ gỡ khi đã chuyển sang Gboard/Laban Key."),
        "com.iflytek.inputmethod.miui" to c("Bàn phím iFlytek", "Chỉ gỡ khi đã chuyển sang Gboard/Laban Key."),

        // ===== App Trung Quốc cài sẵn =====
        "com.taobao.taobao" to s("Taobao", "Mua sắm Trung Quốc."),
        "com.jingdong.app.mall" to s("JD", "Mua sắm Trung Quốc."),
        "com.xunmeng.pinduoduo" to s("Pinduoduo", "Mua sắm Trung Quốc."),
        "com.ss.android.ugc.aweme" to s("Douyin", "TikTok bản Trung Quốc."),
        "com.ss.android.article.news" to s("Toutiao", "Tin tức Trung Quốc."),
        "com.sina.weibo" to s("Weibo", "Mạng xã hội Trung Quốc."),
        "com.zhihu.android" to s("Zhihu", "Hỏi đáp Trung Quốc."),
        "com.UCMobile" to s("UC Browser", "Trình duyệt UC."),
        "com.baidu.searchbox" to s("Baidu", "Tìm kiếm Baidu."),
        "com.autonavi.minimap" to s("Amap", "Bản đồ Trung Quốc."),
        "com.baidu.BaiduMap" to s("Baidu Map", "Bản đồ Baidu."),
        "com.tencent.qqlive" to s("Tencent Video", "Xem phim Trung Quốc."),
        "com.youku.phone" to s("Youku", "Xem phim Trung Quốc."),
        "com.qiyi.video" to s("iQIYI", "Xem phim Trung Quốc."),
        "com.dragon.read" to s("Fanqie Novel", "Đọc truyện Trung Quốc."),
        "com.smile.gifmaker" to s("Kuaishou", "Video ngắn Trung Quốc."),
        "com.tencent.qqmusic" to s("QQ Music", "Nhạc Trung Quốc."),
        "com.netease.cloudmusic" to s("NetEase Music", "Nhạc Trung Quốc."),
        "com.eg.android.AlipayGphone" to c("Alipay", "Gỡ nếu không dùng Alipay."),
        "com.tencent.mm" to c("WeChat", "Gỡ nếu không dùng WeChat."),
        "com.tencent.mobileqq" to s("QQ", "Mạng xã hội Trung Quốc."),
        "com.ctrip.ct" to s("Ctrip", "Du lịch Trung Quốc."),
        "com.sankuai.meituan" to s("Meituan", "Giao đồ ăn Trung Quốc."),
        "me.ele" to s("Ele.me", "Giao đồ ăn Trung Quốc."),
        "com.achievo.vipshop" to s("Vipshop", "Mua sắm Trung Quốc."),
        "com.xingin.xhs" to s("Xiaohongshu", "Mạng xã hội Trung Quốc."),
        "cn.wps.moffice_eng.xiaomi.lite" to s("WPS Lite", "Đọc văn bản bản Xiaomi."),
        "com.wali.live" to s("Mi Live", "Phát trực tiếp."),

        // ===== MIUI bản quốc tế (Global) =====
        "com.mi.globalbrowser" to s("Mi Browser", "Trình duyệt Xiaomi, rất nhiều quảng cáo. Nhớ cài Chrome trước."),
        "com.mi.globalTrendNews" to s("Mi Feed / Tin tức", "Tin tức kèm quảng cáo."),
        "com.miui.videoplayer" to s("Mi Video", "Xem video Xiaomi, nhiều quảng cáo và gợi ý."),
        "com.xiaomi.glgm" to s("Games (Xiaomi)", "Kho game Xiaomi, đẩy quảng cáo game."),
        "com.miui.fmservice" to s("Dịch vụ Radio FM", "Đi kèm Radio FM."),
        "com.xiaomi.mipicks" to c("GetApps", "Kho ứng dụng Xiaomi, hay đẩy quảng cáo. Gỡ thì app hệ thống tự cập nhật qua Google Play."),
        "com.mi.globalminusscreen" to c("App Vault", "Trang tiện ích bên trái màn hình chính. Gỡ thì mất trang này."),
        "com.mi.android.globalFileexplorer" to c("Quản lý tệp", "Trình quản lý file mặc định. Chỉ gỡ khi đã cài app quản lý file khác."),
        "com.xiaomi.midrop" to c("ShareMe", "Chia sẻ file không cần mạng."),
        "com.mipay.wallet.in" to s("Mi Pay (Ấn Độ)", "Ví điện tử bản Ấn Độ."),
        "com.mi.webkit.core" to c("Mi WebView", "Lõi web của app Xiaomi."),
        "com.facebook.appmanager" to s("Facebook App Manager", "Tự cập nhật Facebook cài sẵn ngầm. Không ảnh hưởng app Facebook anh tự cài."),
        "com.facebook.services" to s("Facebook Services", "Dịch vụ nền của Facebook, chạy ngầm và thu thập dữ liệu."),
        "com.facebook.system" to s("Facebook App Installer", "Tự cài app Facebook ngầm."),
        "com.netflix.partner.activation" to s("Netflix kích hoạt", "Dịch vụ Netflix cài sẵn."),
        "com.amazon.appmanager" to s("Amazon App Manager", "Tự cài app Amazon."),
        "com.amazon.mShop.android.shopping" to s("Amazon Shopping", "Mua sắm Amazon cài sẵn."),
        "in.amazon.mShop.android.shopping" to s("Amazon India", "Mua sắm Amazon cài sẵn."),
        "com.booking" to s("Booking.com", "Cài sẵn, gỡ nếu không dùng."),
        "com.agoda.mobile.consumer" to s("Agoda", "Cài sẵn, gỡ nếu không dùng."),
        "com.linkedin.android" to s("LinkedIn", "Cài sẵn, gỡ nếu không dùng."),
        "com.ebay.mobile" to s("eBay", "Cài sẵn, gỡ nếu không dùng."),
        "com.opera.preinstall" to s("Opera cài sẵn", "Trình cài Opera ngầm."),
        "com.opera.browser" to s("Opera", "Trình duyệt cài sẵn."),
        "com.ss.android.ugc.trill" to c("TikTok", "Gỡ nếu không dùng."),
        "com.spotify.music" to c("Spotify", "Gỡ nếu không dùng."),
        "com.netflix.mediaclient" to c("Netflix", "Gỡ nếu không dùng."),
        "com.google.android.apps.tachyon" to s("Google Meet (Duo)", "Gọi video Google, gỡ nếu không dùng."),
        "com.google.android.videos" to s("Google TV", "Thuê phim Google."),
        "com.google.android.apps.podcasts" to s("Google Podcasts", "Đã ngừng hoạt động."),
        "com.google.android.apps.subscriptions.red" to s("Google One", "Quản lý gói lưu trữ Google."),
        "com.google.android.feedback" to s("Google Feedback", "Gửi phản hồi cho Google."),
        "com.google.ar.lens" to s("Google Lens", "Đã có sẵn trong ứng dụng Google."),
        "com.google.android.apps.googleassistant" to c("Google Assistant (lối tắt)", "Lối tắt trợ lý Google."),
        "com.google.android.apps.youtube.music" to c("YouTube Music", "Gỡ nếu không nghe nhạc trên YouTube Music."),
        "com.google.android.apps.wellbeing" to c("Digital Wellbeing", "Thống kê thời gian dùng máy."),
        "com.google.android.apps.photos" to c("Google Photos", "Có ảnh sao lưu thì đừng gỡ."),
        "com.google.android.apps.docs" to c("Google Drive", "Gỡ nếu không dùng Drive."),
        "com.google.android.gm" to c("Gmail", "Gỡ nếu không dùng Gmail."),
        "com.google.android.youtube" to c("YouTube", "Gỡ nếu không dùng."),
        "com.google.android.apps.maps" to c("Google Maps", "Gỡ nếu không dùng."),
        "com.google.android.googlequicksearchbox" to c("Google", "Thanh tìm kiếm & trợ lý Google."),
        "com.miui.global.packageinstaller" to x("Trình cài ứng dụng", "Gỡ thì không cài APK được nữa."),
        "com.xiaomi.micloud.sdk" to c("Xiaomi Cloud SDK", "Đồng bộ đám mây."),

        // ===== TUYỆT ĐỐI KHÔNG GỠ =====
        "android" to x("Hệ điều hành Android", "Lõi hệ thống."),
        "com.android.systemui" to x("Giao diện hệ thống", "Thanh trạng thái, thông báo."),
        "com.android.settings" to x("Cài đặt", "Ứng dụng Cài đặt."),
        "com.miui.home" to x("Màn hình chính", "Launcher HyperOS."),
        "com.miui.securitycenter" to x("Bảo mật", "Gỡ có thể gây treo logo (bootloop)."),
        "com.miui.securityadd" to x("Bảo mật – bổ sung", "Đi kèm Bảo mật."),
        "com.miui.securitycore" to x("Bảo mật – lõi", "Đi kèm Bảo mật."),
        "com.miui.guardprovider" to x("Guard Provider", "Quét an toàn khi cài ứng dụng."),
        "com.lbe.security.miui" to x("Quản lý quyền", "Quản lý quyền ứng dụng của Xiaomi."),
        "com.miui.system" to x("MIUI System", "Tài nguyên hệ thống."),
        "com.miui.rom" to x("MIUI ROM", "Tài nguyên hệ thống."),
        "com.miui.core" to x("MIUI Core", "Thư viện lõi."),
        "com.xiaomi.xmsf" to x("Xiaomi Service Framework", "Thông báo đẩy, đồng bộ."),
        "com.xiaomi.account" to x("Tài khoản Xiaomi", "Đăng nhập Mi Account."),
        "com.xiaomi.finddevice" to x("Tìm thiết bị", "Gỡ có thể khoá máy."),
        "com.miui.powerkeeper" to x("Quản lý pin", "Gỡ làm hỏng tối ưu pin."),
        "com.miui.packageinstaller" to x("Trình cài ứng dụng", "Gỡ thì không cài APK được nữa."),
        "com.android.packageinstaller" to x("Trình cài ứng dụng", "Cài/gỡ ứng dụng."),
        "com.google.android.packageinstaller" to x("Trình cài ứng dụng", "Cài/gỡ ứng dụng."),
        "com.android.permissioncontroller" to x("Quản lý quyền", "Cấp quyền ứng dụng."),
        "com.google.android.permissioncontroller" to x("Quản lý quyền", "Cấp quyền ứng dụng."),
        "com.android.phone" to x("Điện thoại (dịch vụ)", "Gọi điện, SIM."),
        "com.android.incallui" to x("Màn hình cuộc gọi", "Giao diện khi gọi."),
        "com.android.contacts" to x("Danh bạ & Gọi điện", "Danh bạ và quay số."),
        "com.android.mms" to x("Tin nhắn", "SMS, mã OTP ngân hàng."),
        "com.android.camera" to x("Máy ảnh", "Camera Xiaomi."),
        "com.android.deskclock" to c("Đồng hồ / Báo thức", "Gỡ thì mất báo thức."),
        "com.miui.misettings" to x("Cài đặt bổ sung", "Một số trang Cài đặt nằm ở đây."),
        "com.xiaomi.misettings" to x("Cài đặt bổ sung", "Một số trang Cài đặt nằm ở đây."),
        "com.android.vending" to x("Google Play", "Kho ứng dụng Google."),
        "com.google.android.gms" to x("Google Play Services", "Cần cho mọi app Google."),
        "com.google.android.gsf" to x("Google Services Framework", "Cần cho mọi app Google."),
        "com.google.android.webview" to x("WebView", "Nhiều ứng dụng cần để hiển thị web."),
        "com.android.webview" to x("WebView", "Nhiều ứng dụng cần để hiển thị web."),
        "com.android.inputdevices" to x("Thiết bị nhập", "Bàn phím vật lý, chuột."),
        "com.android.nfc" to x("NFC", "Kết nối NFC."),
        "com.android.bluetooth" to x("Bluetooth", "Kết nối Bluetooth."),
        "com.android.shell" to x("Shell", "Cần cho Shizuku và ADB."),
        "com.android.documentsui" to x("Trình chọn tệp", "Mở/lưu tệp trong các app."),
        "com.android.externalstorage" to x("Bộ nhớ ngoài", "Truy cập bộ nhớ."),
        "com.android.mtp" to x("MTP", "Kết nối máy tính qua cáp."),
        "com.android.server.telecom" to x("Telecom", "Quản lý cuộc gọi."),
        "com.android.stk" to x("SIM Toolkit", "Ứng dụng của nhà mạng trên SIM."),
        "com.miui.securityinputmethod" to x("Bàn phím bảo mật", "Bàn phím khi nhập mật khẩu."),
        "moe.shizuku.privileged.api" to x("Shizuku", "Cần cho COD Cleaner hoạt động."),
        BuildConfigShim.APP_ID to x("COD Cleaner", "Ứng dụng này.")
    )

    /** Tiền tố hệ thống: mặc định là không được gỡ. */
    private val criticalPrefixes = listOf(
        "com.android.providers.", "com.qualcomm.", "com.qti.", "vendor.", "org.codeaurora.",
        "com.android.networkstack", "com.android.cellbroadcast", "com.android.wifi",
        "com.android.se", "com.android.keychain", "com.android.location", "com.android.carrierconfig",
        "com.google.android.ext.", "com.android.adservices", "com.google.android.adservices",
        "com.android.ondevicepersonalization", "com.android.healthconnect", "com.android.overlay",
        "com.miui.miwallpaper", "com.xiaomi.bluetooth", "com.android.uwb"
    )

    fun rule(pkg: String, isSystem: Boolean): Rule? {
        map[pkg]?.let { return it }
        if (pkg.endsWith(".overlay") || pkg.contains(".overlay.")) {
            return Rule(Risk.CRITICAL, "", "Gói giao diện hệ thống (overlay).")
        }
        if (criticalPrefixes.any { pkg.startsWith(it) }) {
            return Rule(Risk.CRITICAL, "", "Thành phần lõi của hệ thống.")
        }
        return null
    }

    fun knownSafe(): Set<String> = map.filterValues { it.risk == Risk.SAFE }.keys

    /** Dịch vụ quảng cáo / theo dõi của MIUI – gỡ để tắt quảng cáo trong app hệ thống. */
    val adServices = listOf(
        "com.miui.msa.global", "com.miui.systemAdSolution", "com.miui.analytics", "com.miui.daemon",
        "com.miui.hybrid", "com.miui.hybrid.accessory", "com.mi.globalTrendNews", "com.miui.newhome",
        "com.miui.contentextension", "com.miui.android.fashiongallery", "com.mfashiongallery.emag",
        "com.facebook.appmanager", "com.facebook.services", "com.facebook.system",
        "com.amazon.appmanager", "com.opera.preinstall", "com.xiaomi.ab"
    )
}

object BuildConfigShim {
    const val APP_ID = "vn.daopham.codcleaner"
}
