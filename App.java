import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    // نموذج المستخدم
    static class User {
        String name;
        String email;
        String password;
        String phone;
        String address;

        User(String name, String email, String password, String phone, String address) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.phone = phone;
            this.address = address;
        }
    }

    // تعريف المنتج
    static class Product {
        int id;
        String name;
        int price;
        int oldPrice;
        String badge;
        String description;
        String mainImage;
        List<String> images;
        List<String> options;

        Product(int id, String name, int price, int oldPrice, String badge, String description, String mainImage, List<String> images, List<String> options) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.oldPrice = oldPrice;
            this.badge = badge;
            this.description = description;
            this.mainImage = mainImage;
            this.images = images;
            this.options = options;
        }
    }

    // عنصر السلة
    static class CartItem {
        int productId;
        String productName;
        String option;
        int price;
        String image;

        CartItem(int productId, String productName, String option, int price, String image) {
            this.productId = productId;
            this.productName = productName;
            this.option = option;
            this.price = price;
            this.image = image;
        }
    }

    // نموذج الطلب
    static class Order {
        String name;
        String email;
        String phone;
        String address;
        String itemDetails;
        int total;
        String notes;
        String date;

        Order(String name, String email, String phone, String address, String itemDetails, int total, String notes, String date) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.itemDetails = itemDetails;
            this.total = total;
            this.notes = notes;
            this.date = date;
        }
    }

    static final Map<Integer, Product> products = new HashMap<>();
    static final Map<String, User> users = new ConcurrentHashMap<>();
    static final Map<String, String> sessions = new ConcurrentHashMap<>();
    static final Map<String, List<CartItem>> carts = new ConcurrentHashMap<>();
    static final List<Order> orders = Collections.synchronizedList(new ArrayList<>());

    static {
        // 1. ساعة MSTIANQ الأصلية
        products.put(1, new Product(
            1,
            "ساعة MSTIANQ الكلاسيكية المربعة",
            40,
            85,
            "الأكثر طلباً • أصلي 100%",
            "ساعة كلاسيكية فاخرة بمينا مربع بلمسة خشبية وسوار جلدي بني عالي الجودة ومريح، ضد الماء وضد الخدش.",
            "https://i.postimg.cc/p5d2dPtV/1789793034075.png",
            Arrays.asList(
                "https://i.postimg.cc/p5d2dPtV/1789793034075.png",
                "https://i.postimg.cc/vghYMck4/1789793053818.png",
                "https://i.postimg.cc/kR5n57dg/1789793039841.png",
                "https://i.postimg.cc/V0NYNzcY/1789793044715.png",
                "https://i.postimg.cc/fSbwbMhT/1789793049592.png"
            ),
            Arrays.asList("بني كلاسيكي (خشبي)", "أسود ملكي كامل", "كحلي كلاسيكي", "رمادي معدني")
        ));

        // 2. محفظة بطاقات ونقود ذكية
        products.put(2, new Product(
            2,
            "محفظة ذكية من ألياف الكربون مع حماية RFID",
            35,
            70,
            "الأعلى مبيعاً",
            "محفظة عصرية نحيفة من ألياف الكربون مع نظام إخراج البطاقات السريع وحماية كاملة ضد سرقة بيانات البطاقات الائتمانية.",
            "https://images.unsplash.com/photo-1627123424574-724758594e93?w=800&auto=format&fit=crop&q=80",
            Arrays.asList("https://images.unsplash.com/photo-1627123424574-724758594e93?w=800&auto=format&fit=crop&q=80"),
            Arrays.asList("أسود كربون مات", "رمادي تيتانيوم", "فضي معدني")
        ));

        // 3. نظارة شمسية كلاسيكية مستقطبة
        products.put(3, new Product(
            3,
            "نظارة شمسية بولارايزد كلاسيكية UV400",
            45,
            95,
            "إصدار خاص",
            "إطار معدني خفيف الوزن مع عدسات بولارايزد عاكسة ومضادة للأشعة فوق البنفسجية لحماية العين وأناقة استثنائية.",
            "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&auto=format&fit=crop&q=80",
            Arrays.asList("https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&auto=format&fit=crop&q=80"),
            Arrays.asList("إطار أسود مع عدسات سوداء", "إطار ذهبي مع عدسات خضراء كلاسيكية", "إطار فضي")
        ));

        // 4. قلم معدني فاخر متعدد الوظائف
        products.put(4, new Product(
            4,
            "طقم قلم تنفيذي فاخر من التيتانيوم",
            25,
            55,
            "عرض مميز",
            "قلم مكتب وتوقيع فاخر مصنوع من سبائك الألومنيوم والتيتانيوم المتينة مع كتابة ناعمة وحبر ألماني قابل لإعادة التعبئة.",
            "https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=800&auto=format&fit=crop&q=80",
            Arrays.asList("https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=800&auto=format&fit=crop&q=80"),
            Arrays.asList("أسود ملكي مع حواف ذهبية", "فضي مطفي", "رمادي حديدي")
        ));

        // 5. سماعات بلوتوث ميني بخاصية عزل الضوضاء
        products.put(5, new Product(
            5,
            "سماعات أذن لاسلكية TWS بتقنية Hi-Fi",
            55,
            120,
            "تكنولوجيا متطورة",
            "صوت نقي ثلاثي الأبعاد مع بطارية تدوم حتى 28 ساعة وشاشة رقمية لعرض نسبة الشحن ومقاومة لرذاذ الماء والتعرق.",
            "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80",
            Arrays.asList("https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&auto=format&fit=crop&q=80"),
            Arrays.asList("أسود بيانو فخم", "أبيض سيراميك")
        ));

        // 6. شاحن لاسلكي مكتبي سريع 3 في 1
        products.put(6, new Product(
            6,
            "محطة شحن لاسلكي مغناطيسية 3 في 1",
            65,
            140,
            "اختيار المحترفين",
            "قاعدة شحن أنيقة وقابلة للطي تشحن الهاتف، الساعة الذكية، والسماعات في وقت واحد وبسرعة شحن تصل إلى 15 واط.",
            "https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=800&auto=format&fit=crop&q=80",
            Arrays.asList("https://images.unsplash.com/photo-1622445262464-84b1456045b6?w=800&auto=format&fit=crop&q=80"),
            Arrays.asList("أسود فاخر", "أبيض عصري")
        ));
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (Exception ignored) {}
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/products", new ProductsCatalogHandler());
        server.createContext("/product", new ProductDetailHandler());
        server.createContext("/cart", new CartHandler());
        server.createContext("/add-to-cart", new AddToCartHandler());
        server.createContext("/checkout", new CheckoutHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/account", new AccountHandler());
        server.createContext("/admin", new AdminHandler());
        server.setExecutor(null);

        System.out.println("==========================================");
        System.out.println("متجر رِواق (REWAQ) الشامل يعمل بنجاح!");
        System.out.println("الرابط: http://localhost:" + port);
        System.out.println("==========================================");

        server.start();
    }

    static String getSessionToken(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String c : cookieHeader.split(";")) {
                String[] pair = c.trim().split("=");
                if (pair.length == 2 && "rewaq_session".equals(pair[0])) {
                    return pair[1];
                }
            }
        }
        return null;
    }

    static User getLoggedInUser(HttpExchange exchange) {
        String token = getSessionToken(exchange);
        if (token != null) {
            String email = sessions.get(token);
            if (email != null) return users.get(email);
        }
        return null;
    }

    static String ensureSession(HttpExchange exchange) {
        String token = getSessionToken(exchange);
        if (token == null) {
            token = UUID.randomUUID().toString();
            exchange.getResponseHeaders().add("Set-Cookie", "rewaq_session=" + token + "; Path=/; HttpOnly");
        }
        return token;
    }

    static Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    static void sendResponse(HttpExchange exchange, int statusCode, String responseHtml) throws IOException {
        byte[] bytes = responseHtml.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static String renderHeader(User user) {
        String nav = (user != null)
            ? "<div class='user-menu'><span>مرحباً، " + user.name + "</span><a href='/products' class='btn-nav'>🛍️ المنتجات</a><a href='/account' class='btn-nav'>حسابي</a><a href='/cart' class='btn-nav cart-btn'>🛒 السلة</a><a href='/logout' class='btn-nav logout'>خروج</a></div>"
            : "<div class='user-menu'><a href='/products' class='btn-nav'>🛍️ المنتجات</a><a href='/cart' class='btn-nav cart-btn'>🛒 السلة</a><a href='/login' class='btn-nav'>تسجيل الدخول</a><a href='/register' class='btn-nav register'>إنشاء حساب</a></div>";
        return "<header><a href='/' class='logo'><span class='logo-icon'></span> رِواق | REWAQ</a>" + nav + "</header>";
    }

    static String getCommonStyles() {
        return """
            :root { --primary: #c5a059; --primary-dark: #a37f3b; --bg: #0d0f12; --card-bg: #15181e; --card-border: #232732; --text: #ffffff; --text-muted: #8e95a5; }
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Cairo', sans-serif; }
            body { background-color: var(--bg); color: var(--text); line-height: 1.6; }
            header { background: #111318; border-bottom: 1px solid var(--card-border); padding: 16px 28px; display: flex; justify-content: space-between; align-items: center; position: sticky; top: 0; z-index: 100; backdrop-filter: blur(10px); }
            .logo { font-size: 24px; font-weight: 800; color: var(--primary); text-decoration: none; display: flex; align-items: center; gap: 8px; }
            .logo-icon { width: 14px; height: 22px; border: 2.5px solid var(--primary); border-top-left-radius: 12px; border-top-right-radius: 12px; display: inline-block; }
            .user-menu { display: flex; align-items: center; gap: 8px; font-size: 14px; flex-wrap: wrap; }
            .btn-nav { padding: 7px 15px; border-radius: 8px; text-decoration: none; color: #fff; background: #1f232c; border: 1px solid var(--card-border); font-size: 13px; font-weight: 600; transition: 0.2s; }
            .btn-nav.register { background: var(--primary); color: #000; border-color: var(--primary); }
            .btn-nav.cart-btn { background: rgba(197, 160, 89, 0.15); color: var(--primary); border-color: var(--primary); }
            .btn-nav.logout { background: #3b1d24; border-color: #5e2632; color: #ff9aa2; }
            .container { max-width: 1100px; margin: 35px auto; padding: 0 16px; }
            """;
    }

    // 1. الصفحة الأولى: الواجهة الترحيبية
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            ensureSession(exchange);
            User user = getLoggedInUser(exchange);

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            sb.append("<title>رِواق | REWAQ - للأناقة عنوان</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800;900&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append(".hero { display: flex; align-items: center; justify-content: center; text-align: center; padding: 70px 20px; background: radial-gradient(circle at center, rgba(197, 160, 89, 0.08) 0%, transparent 70%); }");
            sb.append(".hero-content { max-width: 750px; }");
            sb.append(".hero-badge { display: inline-block; background: rgba(197, 160, 89, 0.12); color: var(--primary); padding: 6px 18px; border-radius: 30px; font-size: 14px; font-weight: 700; margin-bottom: 22px; border: 1px solid rgba(197, 160, 89, 0.25); }");
            sb.append("h1 { font-size: 46px; font-weight: 900; line-height: 1.25; margin-bottom: 20px; }");
            sb.append("h1 span { color: var(--primary); }");
            sb.append(".hero-desc { font-size: 18px; color: var(--text-muted); margin-bottom: 35px; line-height: 1.8; }");
            sb.append(".cta-btn { display: inline-flex; align-items: center; gap: 10px; background: var(--primary); color: #000; padding: 16px 38px; border-radius: 12px; font-size: 18px; font-weight: 800; text-decoration: none; transition: 0.3s; box-shadow: 0 10px 25px rgba(197, 160, 89, 0.3); }");
            sb.append(".cta-btn:hover { background: var(--primary-dark); transform: translateY(-3px); }");
            sb.append(".features-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; max-width: 1000px; margin: 0 auto 50px auto; padding: 0 20px; }");
            sb.append(".feat-card { background: var(--card-bg); border: 1px solid var(--card-border); padding: 22px; border-radius: 16px; text-align: center; }");
            sb.append(".feat-title { font-weight: 700; font-size: 16px; margin-bottom: 6px; color: #fff; }");
            sb.append(".feat-desc { font-size: 13px; color: var(--text-muted); }");
            sb.append("footer { text-align: center; padding: 25px; color: var(--text-muted); font-size: 13px; border-top: 1px solid var(--card-border); background: #111318; margin-top: 40px; }");
            sb.append("</style></head><body>");

            sb.append(renderHeader(user));
            sb.append("<div class='hero'><div class='hero-content'>");
            sb.append("<div class='hero-badge'>متجر رِواق للإكسسوارات الفاخرة</div>");
            sb.append("<h1>إطلالتك تبدأ من <span>رِواق</span>، فخامة تليق بك</h1>");
            sb.append("<p class='hero-desc'>تشكيلة مختارة بعناية من الساعات الكلاسيكية، المحافظ الذكية، والإكسسوارات العصرية بأفضل الأسعار مباشرة مع الدفع عند الاستلام.</p>");
            sb.append("<a href='/products' class='cta-btn'>تصفح كتالوج المنتجات الآن 🛍️</a>");
            sb.append("</div></div>");

            sb.append("<div class='features-grid'>");
            sb.append("<div class='feat-card'><h3 class='feat-title'>✨ جودة مختارة</h3><p class='feat-desc'>منتجات أصلية بمواصفات عالية</p></div>");
            sb.append("<div class='feat-card'><h3 class='feat-title'>🚚 الدفع عند الاستلام</h3><p class='feat-desc'>عاين طلبك أولاً عند باب المنزل</p></div>");
            sb.append("<div class='feat-card'><h3 class='feat-title'>📦 شحن سريع ومباشر</h3><p class='feat-desc'>توصيل لكافة مدن ومناطق المملكة</p></div>");
            sb.append("</div>");

            sb.append("<footer>جميع الحقوق محفوظة © متجر رِواق (REWAQ)</footer>");
            sb.append("</body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    // 2. صفحة كتالوج المنتجات المتعددة
    static class ProductsCatalogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ensureSession(exchange);
            User user = getLoggedInUser(exchange);

            StringBuilder grid = new StringBuilder();
            for (Product p : products.values()) {
                grid.append("<div class='product-card'>")
                    .append("<div class='img-box'><img src='").append(p.mainImage).append("' alt='").append(p.name).append("'>")
                    .append("<span class='p-badge'>").append(p.badge).append("</span></div>")
                    .append("<div class='p-body'>")
                    .append("<h3>").append(p.name).append("</h3>")
                    .append("<p class='p-desc'>").append(p.description).append("</p>")
                    .append("<div class='p-price-row'>")
                    .append("<span class='p-price'>").append(p.price).append(" ر.س</span>")
                    .append("<span class='p-old-price'>").append(p.oldPrice).append(" ر.س</span>")
                    .append("</div>")
                    .append("<a href='/product?id=").append(p.id).append("' class='btn-view'>معاينة وشراء 🛍️</a>")
                    .append("</div></div>");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            sb.append("<title>المنتجات | متجر رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append(".catalog-header { text-align: center; margin-bottom: 35px; }");
            sb.append(".catalog-header h1 { font-size: 32px; font-weight: 800; color: var(--primary); margin-bottom: 8px; }");
            sb.append(".products-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 26px; }");
            sb.append(".product-card { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 18px; overflow: hidden; display: flex; flex-direction: column; transition: 0.3s; }");
            sb.append(".product-card:hover { transform: translateY(-5px); border-color: var(--primary); }");
            sb.append(".img-box { position: relative; width: 100%; aspect-ratio: 1/1; background: #000; overflow: hidden; }");
            sb.append(".img-box img { width: 100%; height: 100%; object-fit: cover; transition: 0.3s ease; }");
            sb.append(".product-card:hover .img-box img { transform: scale(1.05); }");
            sb.append(".p-badge { position: absolute; top: 12px; right: 12px; background: rgba(15,17,21,0.85); color: var(--primary); border: 1px solid var(--primary); padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; backdrop-filter: blur(5px); }");
            sb.append(".p-body { padding: 20px; display: flex; flex-direction: column; flex: 1; }");
            sb.append(".p-body h3 { font-size: 17px; margin-bottom: 8px; line-height: 1.4; color: #fff; }");
            sb.append(".p-desc { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; flex: 1; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }");
            sb.append(".p-price-row { display: flex; align-items: baseline; gap: 10px; margin-bottom: 16px; }");
            sb.append(".p-price { font-size: 22px; font-weight: 800; color: var(--primary); }");
            sb.append(".p-old-price { font-size: 14px; color: var(--text-muted); text-decoration: line-through; }");
            sb.append(".btn-view { text-align: center; background: #1f232c; color: #fff; border: 1px solid var(--card-border); padding: 11px; border-radius: 10px; text-decoration: none; font-size: 14px; font-weight: 700; transition: 0.2s; }");
            sb.append(".btn-view:hover { background: var(--primary); color: #000; border-color: var(--primary); }");
            sb.append("</style></head><body>");

            sb.append(renderHeader(user));
            sb.append("<div class='container'>");
            sb.append("<div class='catalog-header'><h1>تشكيلة منتجات رِواق الفاخرة</h1><p style='color:var(--text-muted);'>اختر منتجك المفضل وسيصلك مع خيار الدفع عند الاستلام</p></div>");
            sb.append("<div class='products-grid'>").append(grid).append("</div>");
            sb.append("</div></body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    // 3. صفحة المنتج الفردي بالتفصيل والصور وخيار الإضافة للسلة
    static class ProductDetailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            ensureSession(exchange);
            User user = getLoggedInUser(exchange);

            String query = exchange.getRequestURI().getQuery();
            int pId = 1;
            if (query != null && query.contains("id=")) {
                try {
                    pId = Integer.parseInt(query.split("id=")[1].split("&")[0]);
                } catch (Exception ignored) {}
            }

            Product prod = products.getOrDefault(pId, products.get(1));

            StringBuilder thumbs = new StringBuilder();
            for (int i = 0; i < prod.images.size(); i++) {
                String img = prod.images.get(i);
                String active = (i == 0) ? "active" : "";
                thumbs.append("<img class='thumb ").append(active).append("' src='").append(img).append("' onclick='setImg(this.src, this)'>");
            }

            StringBuilder optionsHtml = new StringBuilder();
            for (String opt : prod.options) {
                optionsHtml.append("<option value='").append(opt).append("'>").append(opt).append("</option>");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            sb.append("<title>").append(prod.name).append(" | رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append(".back-btn { display: inline-block; color: var(--text-muted); text-decoration: none; margin-bottom: 20px; font-size: 14px; font-weight: 600; }");
            sb.append(".back-btn:hover { color: var(--primary); }");
            sb.append(".product-wrapper { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 24px; padding: 32px; }");
            sb.append("@media (max-width: 850px) { .product-wrapper { grid-template-columns: 1fr; padding: 20px; } }");
            sb.append(".gallery { display: flex; flex-direction: column; gap: 15px; }");
            sb.append(".main-img-container { border-radius: 18px; overflow: hidden; border: 1px solid var(--card-border); background: #000; cursor: zoom-in; }");
            sb.append(".main-image { width: 100%; height: auto; max-height: 480px; object-fit: contain; display: block; }");
            sb.append(".thumbnails { display: flex; gap: 12px; overflow-x: auto; padding-bottom: 6px; }");
            sb.append(".thumb { width: 75px; height: 75px; border-radius: 12px; object-fit: cover; cursor: pointer; border: 2px solid transparent; opacity: 0.7; }");
            sb.append(".thumb:hover, .thumb.active { border-color: var(--primary); opacity: 1; }");
            sb.append(".badge { display: inline-block; background: rgba(197, 160, 89, 0.15); color: var(--primary); padding: 5px 14px; border-radius: 30px; font-size: 13px; font-weight: 700; margin-bottom: 12px; }");
            sb.append("h1 { font-size: 28px; font-weight: 800; margin-bottom: 12px; }");
            sb.append(".price-box { display: flex; align-items: baseline; gap: 14px; margin: 15px 0 22px 0; }");
            sb.append(".current-price { font-size: 32px; font-weight: 800; color: var(--primary); }");
            sb.append(".old-price { font-size: 19px; color: var(--text-muted); text-decoration: line-through; }");
            sb.append(".add-cart-box { background: #101217; padding: 24px; border-radius: 16px; border: 1px solid var(--card-border); margin-top: 24px; }");
            sb.append(".form-group { margin-bottom: 16px; } label { display: block; font-size: 13px; margin-bottom: 6px; color: #b8bfcc; }");
            sb.append("select { width: 100%; padding: 12px 14px; border-radius: 8px; border: 1px solid var(--card-border); background: #1a1e26; color: #fff; font-size: 14px; outline: none; }");
            sb.append(".submit-btn { width: 100%; padding: 14px; background: var(--primary); border: none; border-radius: 10px; color: #000; font-size: 17px; font-weight: 800; cursor: pointer; transition: 0.2s; }");
            sb.append(".submit-btn:hover { background: var(--primary-dark); }");
            sb.append(".modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.9); align-items: center; justify-content: center; }");
            sb.append(".modal img { max-width: 90%; max-height: 90%; border-radius: 12px; }");
            sb.append("</style></head><body>");

            sb.append(renderHeader(user));
            sb.append("<div class='container'>");
            sb.append("<a href='/products' class='back-btn'>← العودة لكافة المنتجات</a>");
            sb.append("<div class='product-wrapper'>");

            // المعرض
            sb.append("<div class='gallery'>");
            sb.append("<div class='main-img-container' onclick='openModal()'>");
            sb.append("<img id='mainImg' class='main-image' src='").append(prod.mainImage).append("' alt='").append(prod.name).append("'>");
            sb.append("</div>");
            sb.append("<div class='thumbnails'>").append(thumbs).append("</div>");
            sb.append("</div>");

            // التفاصيل والشراء
            sb.append("<div class='details'>");
            sb.append("<span class='badge'>").append(prod.badge).append("</span>");
            sb.append("<h1>").append(prod.name).append("</h1>");
            sb.append("<p style='color: var(--text-muted); font-size: 14px;'>").append(prod.description).append("</p>");
            sb.append("<div class='price-box'>");
            sb.append("<span class='current-price'>").append(prod.price).append(" ر.س</span>");
            sb.append("<span class='old-price'>").append(prod.oldPrice).append(" ر.س</span>");
            sb.append("</div>");

            sb.append("<div class='add-cart-box'>");
            sb.append("<form action='/add-to-cart' method='POST'>");
            sb.append("<input type='hidden' name='productId' value='").append(prod.id).append("'>");
            sb.append("<div class='form-group'><label>حدد الخيار أو اللون المفضل:</label>");
            sb.append("<select name='option'>").append(optionsHtml).append("</select></div>");
            sb.append("<button type='submit' class='submit-btn'>أضف إلى السلة وانتقل للشراء 🛒</button>");
            sb.append("</form></div>");

            sb.append("</div></div></div>");

            sb.append("<div id='imgModal' class='modal' onclick='this.style.display=\"none\"'><img id='modalImg' src=''></div>");
            sb.append("<script>");
            sb.append("function setImg(src, el) { document.getElementById('mainImg').src = src; document.querySelectorAll('.thumb').forEach(t => t.classList.remove('active')); el.classList.add('active'); }");
            sb.append("function openModal() { var m = document.getElementById('imgModal'); document.getElementById('modalImg').src = document.getElementById('mainImg').src; m.style.display = 'flex'; }");
            sb.append("</script>");
            sb.append("</body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    // 4. معالج إضافة المنتج للسلة والانتقال المباشر إليها
    static class AddToCartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String token = ensureSession(exchange);
                Map<String, String> data = parseFormData(exchange);
                int pId = Integer.parseInt(data.getOrDefault("productId", "1"));
                String option = data.getOrDefault("option", "اللون الافتراضي");

                Product prod = products.getOrDefault(pId, products.get(1));
                List<CartItem> cart = carts.computeIfAbsent(token, k -> new ArrayList<>());
                cart.add(new CartItem(prod.id, prod.name, option, prod.price, prod.mainImage));

                exchange.getResponseHeaders().set("Location", "/cart");
                exchange.sendResponseHeaders(302, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 5. صفحة السلة والدفع عند الاستلام
    static class CartHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String token = ensureSession(exchange);
            User user = getLoggedInUser(exchange);
            List<CartItem> cart = carts.getOrDefault(token, new ArrayList<>());

            String defName = (user != null) ? user.name : "";
            String defPhone = (user != null) ? user.phone : "";
            String defAddr = (user != null) ? user.address : "";

            int total = 0;
            StringBuilder itemsHtml = new StringBuilder();
            for (CartItem it : cart) {
                total += it.price;
                itemsHtml.append("<div class='cart-item'>")
                         .append("<img src='").append(it.image).append("' class='cart-thumb'>")
                         .append("<div class='cart-details'>")
                         .append("<h4>").append(it.productName).append("</h4>")
                         .append("<p>الخيار / اللون: ").append(it.option).append("</p>")
                         .append("</div>")
                         .append("<div class='cart-price'>").append(it.price).append(" ر.س</div>")
                         .append("</div>");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            sb.append("<title>سلة المشتريات | متجر رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@400;600;700;800&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append(".box { background: var(--card-bg); border: 1px solid var(--card-border); border-radius: 20px; padding: 30px; margin-bottom: 24px; }");
            sb.append(".cart-item { display: flex; align-items: center; justify-content: space-between; padding: 16px 0; border-bottom: 1px solid var(--card-border); gap: 16px; }");
            sb.append(".cart-thumb { width: 70px; height: 70px; border-radius: 10px; object-fit: cover; }");
            sb.append(".cart-details h4 { font-size: 16px; margin-bottom: 4px; }");
            sb.append(".cart-details p { font-size: 13px; color: var(--text-muted); }");
            sb.append(".cart-price { font-size: 18px; font-weight: 800; color: var(--primary); }");
            sb.append(".total-bar { display: flex; justify-content: space-between; align-items: center; font-size: 20px; font-weight: 800; margin-top: 20px; padding-top: 14px; }");
            sb.append(".form-group { margin-bottom: 14px; } label { display: block; font-size: 13px; margin-bottom: 6px; color: #b8bfcc; }");
            sb.append("input, textarea { width: 100%; padding: 12px 14px; border-radius: 8px; border: 1px solid var(--card-border); background: #1a1e26; color: #fff; font-size: 14px; outline: none; }");
            sb.append(".submit-btn { width: 100%; padding: 14px; background: var(--primary); border: none; border-radius: 10px; color: #000; font-size: 17px; font-weight: 800; cursor: pointer; }");
            sb.append(".empty-msg { text-align: center; padding: 40px 20px; color: var(--text-muted); }");
            sb.append("</style></head><body>");

            sb.append(renderHeader(user));
            sb.append("<div class='container'>");
            sb.append("<h2 style='color:var(--primary); margin-bottom:20px;'>سلة مشترياتك 🛒</h2>");

            if (cart.isEmpty()) {
                sb.append("<div class='box empty-msg'>");
                sb.append("<p style='font-size:18px; margin-bottom:18px;'>سلتك فارغة حالياً</p>");
                sb.append("<a href='/products' class='btn-nav' style='background:var(--primary);color:#000;'>تصفح المنتجات وأضف للسلة</a>");
                sb.append("</div>");
            } else {
                sb.append("<div class='box'>");
                sb.append(itemsHtml);
                sb.append("<div class='total-bar'><span>المجموع الكلي:</span><span style='color:var(--primary);'>").append(total).append(" ر.س</span></div>");
                sb.append("</div>");

                sb.append("<div class='box'>");
                sb.append("<h3 style='margin-bottom:16px; color:var(--primary);'>بيانات التوصيل والشحن (الدفع عند الاستلام 🚚)</h3>");
                sb.append("<form action='/checkout' method='POST'>");
                sb.append("<div class='form-group'><label>الاسم الكامل</label><input type='text' name='name' required value='").append(defName).append("' placeholder='الاسم الكريم'></div>");
                sb.append("<div class='form-group'><label>رقم الجوال</label><input type='tel' name='phone' required value='").append(defPhone).append("' placeholder='05xxxxxxxx'></div>");
                sb.append("<div class='form-group'><label>عنوان التوصيل بالتفصيل</label><input type='text' name='address' required value='").append(defAddr).append("' placeholder='المدينة، اسم الحي، الشارع...'></div>");
                sb.append("<div class='form-group'><label>ملاحظات إضافية للتوصيل (اختياري)</label><textarea name='notes' rows='2' placeholder='أي تعليمات تخص مكان أو وقت الاستلام...'></textarea></div>");
                sb.append("<button type='submit' class='submit-btn'>تأكيد وشراء الطلب الآن 🛍️</button>");
                sb.append("</form></div>");
            }

            sb.append("</div></body></html>");
            sendResponse(exchange, 200, sb.toString());
        }
    }

    // 6. إتمام الطلب وتفريغ السلة
    static class CheckoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String token = ensureSession(exchange);
                User user = getLoggedInUser(exchange);
                List<CartItem> cart = carts.getOrDefault(token, new ArrayList<>());

                if (cart.isEmpty()) {
                    exchange.getResponseHeaders().set("Location", "/products");
                    exchange.sendResponseHeaders(302, -1);
                    return;
                }

                Map<String, String> data = parseFormData(exchange);
                String name = data.getOrDefault("name", "");
                String phone = data.getOrDefault("phone", "");
                String address = data.getOrDefault("address", "");
                String notes = data.getOrDefault("notes", "");
                String email = (user != null) ? user.email : "طلب زائر (بدون حساب)";

                int total = 0;
                StringBuilder details = new StringBuilder();
                for (CartItem it : cart) {
                    total += it.price;
                    details.append(it.productName).append(" [").append(it.option).append("] - ").append(it.price).append(" ر.س | ");
                }

                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
                orders.add(new Order(name, email, phone, address, details.toString(), total, notes, date));

                cart.clear(); // تفريغ السلة

                StringBuilder sb = new StringBuilder();
                sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
                sb.append("<title>تم تأكيد طلبك | REWAQ</title>");
                sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@600;700&display=swap' rel='stylesheet'>");
                sb.append("<style>");
                sb.append("body { background: #0d0f12; color: #fff; font-family: 'Cairo'; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; text-align: center; }");
                sb.append(".box { background: #15181e; border: 1px solid #232732; padding: 40px; border-radius: 20px; max-width: 480px; }");
                sb.append("h2 { color: #c5a059; margin-bottom: 12px; font-weight: 800; } p { color: #8e95a5; font-size: 15px; margin-bottom: 24px; }");
                sb.append("a { display: inline-block; background: #c5a059; color: #000; padding: 11px 26px; border-radius: 8px; text-decoration: none; font-weight: 700; }");
                sb.append("</style></head><body><div class='box'>");
                sb.append("<h2>✅ تم استلام طلبك وتأكيده بنجاح!</h2>");
                sb.append("<p>شكراً لطلبك من متجر رِواق. تم تسجيل تفاصيل السلة وسنتواصل معك قريباً لتوصيل الشحنة والدفع عند الاستلام.</p>");
                sb.append("<a href='/products'>العودة للتسوق</a></div></body></html>");

                sendResponse(exchange, 200, sb.toString());
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // إدارة الحسابات
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> data = parseFormData(exchange);
                String name = data.getOrDefault("name", "").trim();
                String email = data.getOrDefault("email", "").trim().toLowerCase();
                String password = data.getOrDefault("password", "");
                String phone = data.getOrDefault("phone", "").trim();
                String address = data.getOrDefault("address", "").trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    users.put(email, new User(name, email, password, phone, address));
                    String token = UUID.randomUUID().toString();
                    sessions.put(token, email);

                    exchange.getResponseHeaders().add("Set-Cookie", "rewaq_session=" + token + "; Path=/; HttpOnly");
                    exchange.getResponseHeaders().set("Location", "/account");
                    exchange.sendResponseHeaders(302, -1);
                    return;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<title>إنشاء حساب جديد | متجر رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@600;700&display=swap' rel='stylesheet'>");
            sb.append("<style>");
            sb.append("body { background: #0d0f12; color: #fff; font-family: 'Cairo'; display: flex; align-items: center; justify-content: center; min-height: 100vh; margin: 0; padding: 20px 0; }");
            sb.append(".card { background: #15181e; border: 1px solid #232732; padding: 32px; border-radius: 18px; width: 390px; max-width: 90%; }");
            sb.append("h2 { color: #c5a059; margin-bottom: 20px; text-align: center; }");
            sb.append(".field { margin-bottom: 14px; } label { display: block; font-size: 13px; margin-bottom: 5px; color: #8e95a5; }");
            sb.append("input { width: 100%; box-sizing: border-box; padding: 11px; border-radius: 8px; border: 1px solid #232732; background: #101217; color: #fff; font-size: 14px; outline: none; }");
            sb.append("button { width: 100%; padding: 12px; background: #c5a059; border: none; border-radius: 8px; color: #000; font-weight: 700; cursor: pointer; margin-top: 10px; }");
            sb.append(".link { text-align: center; margin-top: 16px; font-size: 13px; color: #8e95a5; } .link a { color: #c5a059; text-decoration: none; }");
            sb.append("</style></head><body><div class='card'>");
            sb.append("<h2>إنشاء حساب في رِواق</h2>");
            sb.append("<form method='POST'>");
            sb.append("<div class='field'><label>الاسم الكامل</label><input type='text' name='name' required placeholder='مجتبى عبدالله'></div>");
            sb.append("<div class='field'><label>البريد الإلكتروني</label><input type='email' name='email' required placeholder='name@example.com'></div>");
            sb.append("<div class='field'><label>كلمة المرور</label><input type='password' name='password' required></div>");
            sb.append("<div class='field'><label>رقم الجوال</label><input type='tel' name='phone' placeholder='05xxxxxxxx'></div>");
            sb.append("<div class='field'><label>العنوان الافتراضي للشحن</label><input type='text' name='address' placeholder='المدينة، الحي...'></div>");
            sb.append("<button type='submit'>تسجيل الحساب</button></form>");
            sb.append("<div class='link'>لديك حساب بالفعل؟ <a href='/login'>تسجيل الدخول</a></div>");
            sb.append("</div></body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String errorMsg = "";
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> data = parseFormData(exchange);
                String email = data.getOrDefault("email", "").trim().toLowerCase();
                String password = data.getOrDefault("password", "");

                User user = users.get(email);
                if (user != null && user.password.equals(password)) {
                    String token = UUID.randomUUID().toString();
                    sessions.put(token, email);

                    exchange.getResponseHeaders().add("Set-Cookie", "rewaq_session=" + token + "; Path=/; HttpOnly");
                    exchange.getResponseHeaders().set("Location", "/products");
                    exchange.sendResponseHeaders(302, -1);
                    return;
                } else {
                    errorMsg = "<div style='color: #ff6b6b; font-size: 13px; text-align: center; margin-bottom: 12px;'>البريد الإلكتروني أو كلمة المرور غير صحيحة</div>";
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<title>تسجيل الدخول | متجر رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@600;700&display=swap' rel='stylesheet'>");
            sb.append("<style>");
            sb.append("body { background: #0d0f12; color: #fff; font-family: 'Cairo'; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }");
            sb.append(".card { background: #15181e; border: 1px solid #232732; padding: 32px; border-radius: 18px; width: 360px; max-width: 90%; }");
            sb.append("h2 { color: #c5a059; margin-bottom: 20px; text-align: center; }");
            sb.append(".field { margin-bottom: 14px; } label { display: block; font-size: 13px; margin-bottom: 5px; color: #8e95a5; }");
            sb.append("input { width: 100%; box-sizing: border-box; padding: 11px; border-radius: 8px; border: 1px solid #232732; background: #101217; color: #fff; font-size: 14px; outline: none; }");
            sb.append("button { width: 100%; padding: 12px; background: #c5a059; border: none; border-radius: 8px; color: #000; font-weight: 700; cursor: pointer; margin-top: 10px; }");
            sb.append(".link { text-align: center; margin-top: 16px; font-size: 13px; color: #8e95a5; } .link a { color: #c5a059; text-decoration: none; }");
            sb.append("</style></head><body><div class='card'>");
            sb.append("<h2>تسجيل الدخول</h2>").append(errorMsg);
            sb.append("<form method='POST'>");
            sb.append("<div class='field'><label>البريد الإلكتروني</label><input type='email' name='email' required></div>");
            sb.append("<div class='field'><label>كلمة المرور</label><input type='password' name='password' required></div>");
            sb.append("<button type='submit'>دخول</button></form>");
            sb.append("<div class='link'>ليس لديك حساب؟ <a href='/register'>أنشئ حسابك الآن</a></div>");
            sb.append("</div></body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Set-Cookie", "rewaq_session=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT");
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
        }
    }

    static class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            User user = getLoggedInUser(exchange);
            if (user == null) {
                exchange.getResponseHeaders().set("Location", "/login");
                exchange.sendResponseHeaders(302, -1);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<title>حسابي الشخصي | متجر رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@600;700&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append(".card { max-width: 600px; margin: 40px auto; background: #15181e; border: 1px solid #232732; padding: 32px; border-radius: 18px; }");
            sb.append("h2 { color: #c5a059; margin-bottom: 24px; font-weight: 800; }");
            sb.append(".info-row { display: flex; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #232732; font-size: 15px; }");
            sb.append(".label { color: #8e95a5; } .val { font-weight: 600; }");
            sb.append(".btn-back { display: inline-block; margin-top: 25px; padding: 11px 22px; background: #c5a059; color: #000; text-decoration: none; border-radius: 8px; font-weight: 700; }");
            sb.append("</style></head><body>");
            sb.append(renderHeader(user));
            sb.append("<div class='container'><div class='card'>");
            sb.append("<h2>بيانات حسابك في رِواق</h2>");
            sb.append("<div class='info-row'><span class='label'>الاسم الكامل:</span><span class='val'>").append(user.name).append("</span></div>");
            sb.append("<div class='info-row'><span class='label'>البريد الإلكتروني:</span><span class='val'>").append(user.email).append("</span></div>");
            sb.append("<div class='info-row'><span class='label'>رقم الجوال:</span><span class='val'>").append(user.phone).append("</span></div>");
            sb.append("<div class='info-row'><span class='label'>العنوان المحفوظ:</span><span class='val'>").append(user.address).append("</span></div>");
            sb.append("<a href='/products' class='btn-back'>العودة للتسوق</a>");
            sb.append("</div></div></body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }

    // 7. لوحة إدارة الطلبات
    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder rows = new StringBuilder();
            for (Order o : orders) {
                rows.append("<tr>")
                    .append("<td>").append(o.date).append("</td>")
                    .append("<td>").append(o.name).append("</td>")
                    .append("<td>").append(o.phone).append("</td>")
                    .append("<td>").append(o.itemDetails).append("</td>")
                    .append("<td>").append(o.total).append(" ر.س</td>")
                    .append("<td>").append(o.address).append("</td>")
                    .append("<td>").append(o.notes).append("</td>")
                    .append("</tr>");
            }

            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html><html lang='ar' dir='rtl'><head><meta charset='UTF-8'>");
            sb.append("<title>لوحة الإدارة | رِواق</title>");
            sb.append("<link href='https://fonts.googleapis.com/css2?family=Cairo:wght@600;700&display=swap' rel='stylesheet'>");
            sb.append("<style>").append(getCommonStyles());
            sb.append("table { width: 100%; border-collapse: collapse; background: #15181e; border-radius: 12px; overflow: hidden; border: 1px solid #232732; }");
            sb.append("th, td { padding: 14px 18px; text-align: right; border-bottom: 1px solid #232732; font-size: 14px; }");
            sb.append("th { background: #101217; color: #c5a059; font-weight: 700; } tr:hover { background: #1b1f28; }");
            sb.append(".back-link { display: inline-block; margin-bottom: 16px; color: #8e95a5; text-decoration: none; font-size: 14px; }");
            sb.append("</style></head><body><div class='container'>");
            sb.append("<a href='/' class='back-link'>← العودة للمتجر</a>");
            sb.append("<h1 style='color:var(--primary); margin-bottom:20px;'>لوحة إدارة الطلبات المباشرة (REWAQ)</h1>");
            sb.append("<table><thead><tr>");
            sb.append("<th>التاريخ</th><th>اسم العميل</th><th>الجوال</th><th>المنتجات المطلوبة</th><th>الإجمالي</th><th>العنوان</th><th>ملاحظات</th>");
            sb.append("</tr></thead><tbody>").append(rows).append("</tbody></table></div></body></html>");

            sendResponse(exchange, 200, sb.toString());
        }
    }
}
