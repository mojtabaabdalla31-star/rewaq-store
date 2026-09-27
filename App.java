import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class App {

    static AtomicInteger pageViews = new AtomicInteger(0);
    static AtomicInteger totalSalesAmount = new AtomicInteger(0);
    static List<Order> ordersList = new ArrayList<>();

    record Order(String customerName, String phone, String city, String color, int amount) {}

    public static void main(String[] args) throws IOException {
        int port = 8080;

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress("0.0.0.0", port), 0);

        server.createContext("/", new StoreHandler());
        server.createContext("/api/order", new OrderHandler());
        server.createContext("/admin", new AdminHandler());

        server.setExecutor(null);

        System.out.println("==================================================");
        System.out.println(" متجر رِواق (REWAQ) يعمل الآن بنجاح!");
        System.out.println(" الرابط المحلي: http://localhost:" + port);
        System.out.println(" رابط لوحة التحكم: http://localhost:" + port + "/admin");
        System.out.println("==================================================");

        server.start();
    }

    // 1. واجهة متجر رِواق
    static class StoreHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (!path.equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }

            pageViews.incrementAndGet();

            String html = """
                <!DOCTYPE html>
                <html lang="ar" dir="rtl">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>رِواق | REWAQ</title>
                    <link rel="preconnect" href="https://fonts.googleapis.com">
                    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
                    <link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@500;700&family=Montserrat:wght@300;500&family=Reem+Kufi:wght@400;600;700&display=swap" rel="stylesheet">
                    <style>
                        * { box-sizing: border-box; }
                        body { background: #FCFAF7; margin: 0; color: #1c1917; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                        
                        header { background: #141414; color: #FCFAF7; padding: 30px 20px 20px; text-align: center; border-bottom: 1px solid #292524; }
                        .logo-box { display: inline-flex; flex-direction: column; align-items: center; }
                        .logo-arch { width: 44px; height: 44px; margin-bottom: 6px; color: #B89047; }
                        .brand-ar { font-family: 'Reem Kufi', sans-serif; font-size: 30px; font-weight: 600; margin: 0; color: #F5F5F4; }
                        .brand-en { font-family: 'Cinzel', serif; font-size: 14px; letter-spacing: 0.35em; color: #B89047; margin: 2px 0 0 0; text-indent: 0.35em; font-weight: 600; }
                        
                        .container { max-width: 950px; margin: 30px auto; padding: 0 20px; }
                        
                        .product-wrapper { display: grid; grid-template-columns: 1.15fr 1fr; gap: 35px; background: white; padding: 30px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.03); border: 1px solid #e7e5e4; }
                        @media (max-width: 768px) { .product-wrapper { grid-template-columns: 1fr; padding: 20px; } }
                        
                        .gallery-section { display: flex; flex-direction: column; gap: 12px; }
                        .main-image-wrap { position: relative; border-radius: 14px; overflow: hidden; background: #1c1917; height: 380px; display: flex; align-items: center; justify-content: center; }
                        .main-product-img { width: 100%; height: 100%; object-fit: contain; }
                        .waterproof-badge { position: absolute; bottom: 12px; right: 12px; background: rgba(20, 20, 20, 0.85); backdrop-filter: blur(4px); color: #B89047; padding: 5px 12px; border-radius: 20px; font-size: 11px; font-weight: bold; border: 1px solid rgba(184, 144, 71, 0.3); }
                        
                        .thumbs-container { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 4px; }
                        .thumb-btn { width: 62px; height: 62px; border-radius: 8px; border: 2px solid #e7e5e4; overflow: hidden; cursor: pointer; padding: 0; background: #141414; flex-shrink: 0; }
                        .thumb-btn.active { border-color: #B89047; }
                        .thumb-btn img { width: 100%; height: 100%; object-fit: cover; }

                        .product-info h2 { font-size: 24px; margin: 0 0 8px 0; color: #1c1917; font-family: 'Reem Kufi', sans-serif; }
                        .price-tag { font-size: 26px; font-weight: bold; color: #B89047; margin-bottom: 12px; }
                        .old-price { font-size: 15px; color: #a8a29e; text-decoration: line-through; margin-right: 8px; font-weight: normal; }
                        .desc { color: #57534e; line-height: 1.6; font-size: 13.5px; margin-bottom: 18px; }

                        .specs-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 20px; font-size: 12px; color: #44403c; }
                        .spec-item { background: #FCFAF7; padding: 8px 10px; border-radius: 8px; border: 1px solid #f5f5f4; }

                        .color-section { margin-bottom: 22px; }
                        .color-section label { display: block; font-weight: bold; margin-bottom: 8px; font-size: 13px; }
                        .color-options { display: flex; flex-wrap: wrap; gap: 8px; }
                        .color-btn { padding: 8px 13px; border: 1.5px solid #e7e5e4; border-radius: 8px; background: white; cursor: pointer; font-size: 12.5px; font-weight: 500; }
                        .color-btn.selected { border-color: #141414; background: #141414; color: #F5F5F4; }

                        .add-btn { width: 100%; background: #141414; color: #B89047; border: 1px solid #B89047; padding: 14px; font-size: 15px; font-weight: bold; border-radius: 10px; cursor: pointer; }
                        .add-btn:hover { background: #B89047; color: white; }

                        #cart-section { display: none; margin-top: 30px; background: white; padding: 25px; border-radius: 16px; border: 2px solid #B89047; }
                        .checkout-form input { width: 100%; padding: 11px; margin-bottom: 10px; border: 1px solid #d6d3d1; border-radius: 8px; font-size: 14px; }
                        .confirm-btn { width: 100%; background: #B89047; color: white; border: none; padding: 14px; font-size: 15px; font-weight: bold; border-radius: 8px; cursor: pointer; }
                    </style>
                </head>
                <body>
                    <header>
                        <div class="logo-box">
                            <svg class="logo-arch" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 88V44C22 28.536 34.536 16 50 16C65.464 16 78 28.536 78 44V88" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
                                <circle cx="50" cy="40" r="3" fill="currentColor"/>
                            </svg>
                            <h1 class="brand-ar">رِواق</h1>
                            <p class="brand-en">REWAQ</p>
                        </div>
                    </header>

                    <div class="container">
                        <div class="product-wrapper">
                            <div class="gallery-section">
                                <div class="main-image-wrap">
                                    <img id="main-view" class="main-product-img" src="https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80" alt="ساعة رِواق">
                                    <div class="waterproof-badge">💧 30M Waterproof</div>
                                </div>
                                <div class="thumbs-container">
                                    <button class="thumb-btn active" onclick="switchImage('https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=800&q=80', this)">
                                        <img src="https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=150&q=80">
                                    </button>
                                    <button class="thumb-btn" onclick="switchImage('https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=800&q=80', this)">
                                        <img src="https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=150&q=80">
                                    </button>
                                    <button class="thumb-btn" onclick="switchImage('https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?auto=format&fit=crop&w=800&q=80', this)">
                                        <img src="https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?auto=format&fit=crop&w=150&q=80">
                                    </button>
                                </div>
                            </div>

                            <div class="product-info">
                                <h2>ساعة رِواق الكلاسيكية المربعة</h2>
                                <div class="price-tag">40 ريال <span class="old-price">85 ريال</span></div>
                                <p class="desc">تصميم كلاسيكي مربع أنيق بحزام جلدي فاخر وزجاج عالي النقاء مقاوم للخدش وحركة كوارتز دقيقة تناسب جميع الأوقات.</p>

                                <div class="specs-grid">
                                    <div class="spec-item">🛡️ زجاج معدني نقي</div>
                                    <div class="spec-item">⚙️ حركة كوارتز دقيقة</div>
                                    <div class="spec-item">💧 مقاومة للماء 3ATM</div>
                                    <div class="spec-item">🔒 غطاء ستانلس ستيل</div>
                                </div>

                                <div class="color-section">
                                    <label>اللون المفضل:</label>
                                    <div class="color-options">
                                        <button type="button" class="color-btn selected" onclick="selectColor(this, 'بني كلاسيكي (تان)')">بني كلاسيكي</button>
                                        <button type="button" class="color-btn" onclick="selectColor(this, 'أسود ملكي')">أسود ملكي</button>
                                        <button type="button" class="color-btn" onclick="selectColor(this, 'أزرق كحلي')">أزرق كحلي</button>
                                        <button type="button" class="color-btn" onclick="selectColor(this, 'أخضر زيتي')">أخضر زيتي</button>
                                        <button type="button" class="color-btn" onclick="selectColor(this, 'رمادي فضي')">رمادي فضي</button>
                                    </div>
                                </div>

                                <button class="add-btn" onclick="addToCart()">أضف إلى السلة 🛒</button>
                            </div>
                        </div>

                        <div id="cart-section">
                            <h3 style="margin:0 0 10px 0;">سلة المشتريات 🛍️</h3>
                            <p style="margin:0 0 15px 0;">اللون المختار: <strong id="cart-color">بني كلاسيكي</strong> | الإجمالي: <strong>40 ريال</strong></p>
                            <div class="checkout-form">
                                <input type="text" id="cust-name" placeholder="الاسم الكريم">
                                <input type="tel" id="cust-phone" placeholder="رقم الجوال">
                                <input type="text" id="cust-city" placeholder="المدينة وعنوان التوصيل">
                                <button class="confirm-btn" onclick="submitOrder()">تأكيد وإرسال الطلب</button>
                            </div>
                        </div>
                    </div>

                    <script>
                        let selectedColor = 'بني كلاسيكي (تان)';

                        function switchImage(src, btn) {
                            document.querySelectorAll('.thumb-btn').forEach(b => b.classList.remove('active'));
                            btn.classList.add('active');
                            document.getElementById('main-view').src = src;
                        }

                        function selectColor(btn, color) {
                            document.querySelectorAll('.color-btn').forEach(b => b.classList.remove('selected'));
                            btn.classList.add('selected');
                            selectedColor = color;
                        }

                        function addToCart() {
                            document.getElementById('cart-color').textContent = selectedColor;
                            const cart = document.getElementById('cart-section');
                            cart.style.display = 'block';
                            cart.scrollIntoView({ behavior: 'smooth' });
                        }

                        async function submitOrder() {
                            const name = document.getElementById('cust-name').value;
                            const phone = document.getElementById('cust-phone').value;
                            const city = document.getElementById('cust-city').value;

                            if (!name || !phone || !city) return alert('يرجى ملء جميع الحقول');

                            const res = await fetch('/api/order', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
                                body: `name=${encodeURIComponent(name)}&phone=${encodeURIComponent(phone)}&city=${encodeURIComponent(city)}&color=${encodeURIComponent(selectedColor)}&amount=40`
                            });

                            if (res.ok) {
                                alert(`شكراً ${name}! تم تسجيل طلبك بنجاح.`);
                                document.getElementById('cart-section').style.display = 'none';
                            }
                        }
                    </script>
                </body>
                </html>
                """;

            sendHtml(exchange, html);
        }
    }

    // 2. معالج الطلبات
    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);

                String[] params = body.toString().split("&");
                String name = "", phone = "", city = "", color = "";
                int amount = 40;

                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2) {
                        String key = kv[0];
                        String val = java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                        switch (key) {
                            case "name" -> name = val;
                            case "phone" -> phone = val;
                            case "city" -> city = val;
                            case "color" -> color = val;
                            case "amount" -> {
                                try { amount = Integer.parseInt(val); } catch (Exception ignored) {}
                            }
                        }
                    }
                }

                ordersList.add(new Order(name, phone, city, color, amount));
                totalSalesAmount.addAndGet(amount);

                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // 3. لوحة تحكم الإدارة
    static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder rows = new StringBuilder();
            if (ordersList.isEmpty()) {
                rows.append("<tr><td colspan='5' style='text-align:center; color:#94a3b8;'>لا توجد طلبات حتى الآن</td></tr>");
            } else {
                for (Order o : ordersList) {
                    rows.append("<tr>")
                        .append("<td>").append(o.customerName()).append("</td>")
                        .append("<td>").append(o.phone()).append("</td>")
                        .append("<td>").append(o.city()).append("</td>")
                        .append("<td><span style='background:#334155; padding:3px 8px; border-radius:4px;'>").append(o.color()).append("</span></td>")
                        .append("<td><strong>").append(o.amount()).append(" ريال</strong></td>")
                        .append("</tr>");
                }
            }

            String html = """
                <!DOCTYPE html>
                <html lang="ar" dir="rtl">
                <head>
                    <meta charset="UTF-8">
                    <title>إدارة رِواق</title>
                    <style>
                        * { box-sizing: border-box; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                        body { background: #0f172a; color: white; margin: 0; padding: 25px; }
                        .kpi-container { display: flex; gap: 15px; margin: 20px 0; }
                        .kpi-card { background: #1e293b; padding: 15px 20px; border-radius: 8px; flex: 1; border-right: 4px solid #B89047; }
                        table { width: 100%; border-collapse: collapse; background: #1e293b; border-radius: 8px; overflow: hidden; margin-top: 15px; }
                        th, td { padding: 12px; text-align: right; border-bottom: 1px solid #334155; }
                        th { background: #243046; color: #94a3b8; }
                        .auth-overlay { position: fixed; inset: 0; background: #0f172a; display: flex; align-items: center; justify-content: center; }
                        .login-box { background: #1e293b; padding: 30px; border-radius: 8px; text-align: center; }
                        .login-box input { padding: 10px; border-radius: 6px; border: 1px solid #475569; width: 180px; text-align: center; margin-bottom: 10px; background: #0f172a; color: white; }
                        .login-box button { background: #B89047; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; }
                    </style>
                </head>
                <body>
                    <div id="auth" class="auth-overlay">
                        <div class="login-box">
                            <h3>لوحة إدارة رِواق</h3>
                            <input type="password" id="pass" placeholder="رمز المرور"><br>
                            <button onclick="verify()">دخول</button>
                        </div>
                    </div>

                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <h1>إحصائيات متجر رِواق 📊</h1>
                        <a href="/" style="color:#B89047; text-decoration:none;">← المتجر</a>
                    </div>

                    <div class="kpi-container">
                        <div class="kpi-card"><div>الزيارات</div><h2>""" + pageViews.get() + """
                        </h2></div>
                        <div class="kpi-card" style="border-color:#22c55e;"><div>المبيعات</div><h2>""" + totalSalesAmount.get() + """
                         ريال</h2></div>
                        <div class="kpi-card" style="border-color:#f59e0b;"><div>الطلبات</div><h2>""" + ordersList.size() + """
                        </h2></div>
                    </div>

                    <h2>سجل طلبات العملاء</h2>
                    <table>
                        <thead><tr><th>الاسم</th><th>الجوال</th><th>المدينة</th><th>اللون</th><th>المبلغ</th></tr></thead>
                        <tbody>""" + rows.toString() + """
                        </tbody>
                    </table>

                    <script>
                        function verify() {
                            if (document.getElementById('pass').value === '1234') {
                                document.getElementById('auth').style.display = 'none';
                            } else {
                                alert('رمز الدخول غير صحيح!');
                            }
                        }
                    </script>
                </body>
                </html>
                """;

            sendHtml(exchange, html);
        }
    }

    private static void sendHtml(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}