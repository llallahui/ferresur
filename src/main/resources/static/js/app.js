(function () {
    'use strict';

    function togglePassword(id) {
        const input = document.getElementById(id);
        if (!input) return;
        input.type = input.type === 'password' ? 'text' : 'password';
    }

    document.querySelectorAll('[data-toggle-password]').forEach(function (button) {
        button.addEventListener('click', function () {
            const id = button.getAttribute('data-toggle-password');
            togglePassword(id);
        });
    });

    const sidebar = document.getElementById('sidebar');
    document.querySelectorAll('[data-sidebar-toggle], [data-mobile-menu]').forEach(function (button) {
        button.addEventListener('click', function () {
            if (!sidebar) return;
            if (window.innerWidth <= 760) sidebar.classList.toggle('open');
            else {
                sidebar.classList.toggle('collapsed');
                const main = document.querySelector('.main');
                if (main) main.classList.toggle('sidebar-collapsed');
            }
        });
    });

    // Búsqueda instantánea de tablas.
    const tableSearch = document.getElementById('tableSearch');
    const salesTable = document.getElementById('ventasTable');
    if (tableSearch && salesTable) {
        tableSearch.addEventListener('input', function () {
            const q = tableSearch.value.trim().toLowerCase();
            salesTable.querySelectorAll('tbody tr').forEach(function (row) {
                if (!row.querySelector('td')) return;
                row.style.display = row.innerText.toLowerCase().includes(q) ? '' : 'none';
            });
        });
    }

    // Confirmación de contraseña para registro y usuarios.
    function setupPasswordMatch(passwordId, confirmId, messageId, formId) {
        const password = document.getElementById(passwordId);
        const confirm = document.getElementById(confirmId);
        const message = document.getElementById(messageId);
        const form = document.getElementById(formId);
        if (!password || !confirm || !form) return;

        function validate() {
            const bothFilled = password.value.length > 0 && confirm.value.length > 0;
            const valid = !bothFilled || password.value === confirm.value;
            if (message && bothFilled && !valid) message.textContent = 'Las contraseñas no coinciden.';
            else if (message && bothFilled && valid && !message.dataset.serverError) message.textContent = '';
            confirm.classList.toggle('invalid', bothFilled && !valid);
            return valid;
        }

        if (message && message.textContent.trim()) message.dataset.serverError = 'true';
        password.addEventListener('input', validate);
        confirm.addEventListener('input', validate);
        form.addEventListener('submit', function (event) {
            if (!validate()) {
                event.preventDefault();
                confirm.focus();
            }
        });
    }

    setupPasswordMatch('passwordRegistro', 'passwordConfirm', 'passwordMatch', 'registerForm');
    setupPasswordMatch('userPassword', 'userPasswordConfirm', null, 'userForm');

    // Carrito de ventas.
    const list = document.getElementById('productList');
    const cartEl = document.getElementById('cart');
    const totalEl = document.getElementById('cartTotal');
    const hidden = document.getElementById('itemsJson');
    const form = document.getElementById('saleForm');
    const empty = document.getElementById('cartEmpty');
    const clearCart = document.getElementById('clearCart');
    const search = document.getElementById('productSearch');

    if (list && cartEl && totalEl && hidden && form) {
        let cart = [];

        function renderCart() {
            cartEl.innerHTML = '';
            if (empty) empty.style.display = cart.length ? 'none' : 'block';

            let total = 0;
            cart.forEach(function (item, index) {
                total += item.price * item.qty;
                const row = document.createElement('div');
                row.className = 'cart-item';
                row.innerHTML =
                    '<div><b></b><small></small></div>' +
                    '<div class="qty"><button type="button" data-index="' + index + '" data-delta="-1">−</button>' +
                    '<input value="' + item.qty + '" readonly aria-label="Cantidad">' +
                    '<button type="button" data-index="' + index + '" data-delta="1">+</button></div>' +
                    '<strong>S/ ' + (item.price * item.qty).toFixed(2) + '</strong>' +
                    '<button type="button" class="remove" data-remove="' + index + '" title="Quitar">×</button>';
                row.querySelector('b').textContent = item.name;
                row.querySelector('small').textContent = 'S/ ' + item.price.toFixed(2) + ' · Máx. ' + item.stock;
                cartEl.appendChild(row);
            });

            totalEl.textContent = 'S/ ' + total.toFixed(2);
            hidden.value = JSON.stringify(cart.map(function (item) {
                return { id: item.id, cantidad: item.qty };
            }));
        }

        list.addEventListener('click', function (event) {
            const button = event.target.closest('.pick-product');
            if (!button) return;
            const id = Number(button.dataset.id);
            const stock = Number(button.dataset.stock);
            if (stock <= 0) {
                alert('Este producto no tiene stock disponible.');
                return;
            }

            const found = cart.find(function (item) { return item.id === id; });
            if (found) {
                if (found.qty < found.stock) found.qty += 1;
                else alert('No puedes superar el stock disponible.');
            } else {
                cart.push({
                    id: id,
                    name: button.dataset.name,
                    code: button.dataset.code,
                    price: Number(button.dataset.price),
                    stock: stock,
                    qty: 1
                });
            }
            renderCart();
        });

        cartEl.addEventListener('click', function (event) {
            const button = event.target.closest('button');
            if (!button) return;

            if (button.dataset.index !== undefined) {
                const index = Number(button.dataset.index);
                const delta = Number(button.dataset.delta);
                const item = cart[index];
                if (!item) return;
                item.qty += delta;
                if (item.qty <= 0) cart.splice(index, 1);
                if (item.qty > item.stock) item.qty = item.stock;
                renderCart();
            }

            if (button.dataset.remove !== undefined) {
                cart.splice(Number(button.dataset.remove), 1);
                renderCart();
            }
        });

        clearCart?.addEventListener('click', function () {
            if (!cart.length) return;
            if (confirm('¿Vaciar todos los productos de la venta?')) {
                cart = [];
                renderCart();
            }
        });

        search?.addEventListener('input', function () {
            const q = search.value.trim().toLowerCase();
            list.querySelectorAll('.pick-product').forEach(function (button) {
                const text = (button.dataset.name + ' ' + button.dataset.code).toLowerCase();
                button.style.display = text.includes(q) ? 'flex' : 'none';
            });
        });

        form.addEventListener('submit', function (event) {
            if (!cart.length) {
                event.preventDefault();
                alert('Agrega al menos un producto a la venta.');
                return;
            }
            const submit = form.querySelector('button[type="submit"]');
            if (submit) {
                submit.disabled = true;
                submit.textContent = 'Registrando...';
            }
        });

        renderCart();
    }

    // Pago de la venta: efectivo, Yape, tarjeta o crédito.
    const tipoPago = document.getElementById('tipoPago');
    const montoRecibido = document.getElementById('montoRecibido');
    const vuelto = document.getElementById('vuelto');
    const creditFields = document.getElementById('creditFields');
    const cashFields = document.getElementById('cashFields');
    const changeBox = document.getElementById('changeBox');
    function getCartTotal() {
        let total = 0;
        document.querySelectorAll('#cart .cart-item').forEach(function(row){
            const text = row.querySelector('strong')?.textContent || '0';
            total += Number(text.replace(/[^0-9.,-]/g,'').replace(',','.')) || 0;
        });
        return total;
    }
    function updatePaymentFields() {
        if (!tipoPago) return;
        const type = tipoPago.value;
        const cash = type === 'EFECTIVO';
        const credit = type === 'CREDITO';
        if (cashFields) cashFields.style.display = cash ? '' : 'none';
        if (changeBox) changeBox.style.display = cash ? '' : 'none';
        if (creditFields) creditFields.style.display = credit ? '' : 'none';
        if (!cash && montoRecibido) montoRecibido.value = '';
        if (vuelto && cash) {
            const diff = (Number(montoRecibido?.value || 0) - getCartTotal());
            vuelto.textContent = 'S/ ' + Math.max(0, diff).toFixed(2);
        }
    }
    tipoPago?.addEventListener('change', updatePaymentFields);
    montoRecibido?.addEventListener('input', updatePaymentFields);
    updatePaymentFields();

    // Gráfico de ventas mensuales con Canvas, sin dependencias externas.
    const canvas = document.getElementById('monthlySalesChart');
    if (canvas) {
        let labels = [];
        let values = [];
        try {
            labels = JSON.parse(canvas.dataset.labels || '[]');
            values = JSON.parse(canvas.dataset.values || '[]').map(Number);
        } catch (e) {
            labels = [];
            values = [];
        }

        function drawMonthlyChart() {
            const rect = canvas.getBoundingClientRect();
            const ratio = window.devicePixelRatio || 1;
            const width = Math.max(300, Math.floor(rect.width));
            const height = Math.max(240, Math.floor(rect.height));
            canvas.width = width * ratio;
            canvas.height = height * ratio;
            const ctx = canvas.getContext('2d');
            ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
            ctx.clearRect(0, 0, width, height);

            const pad = { top: 18, right: 20, bottom: 42, left: 60 };
            const chartW = width - pad.left - pad.right;
            const chartH = height - pad.top - pad.bottom;
            const maxValue = Math.max(1000, Math.ceil((Math.max.apply(null, values.concat([0])) * 1.15) / 1000) * 1000);
            const steps = 4;

            ctx.font = '12px Segoe UI, Arial';
            ctx.textBaseline = 'middle';
            for (let i = 0; i <= steps; i++) {
                const y = pad.top + chartH - chartH * i / steps;
                ctx.strokeStyle = '#edf0f4';
                ctx.setLineDash([3, 4]);
                ctx.beginPath();
                ctx.moveTo(pad.left, y);
                ctx.lineTo(width - pad.right, y);
                ctx.stroke();
                ctx.setLineDash([]);
                ctx.fillStyle = '#9aa5b5';
                ctx.textAlign = 'right';
                ctx.fillText('S/' + Math.round(maxValue * i / steps).toLocaleString('es-PE'), pad.left - 10, y);
            }

            if (!labels.length) return;
            const points = labels.map(function (_, index) {
                const x = labels.length === 1 ? pad.left + chartW / 2 : pad.left + chartW * index / (labels.length - 1);
                const value = Number(values[index] || 0);
                const y = pad.top + chartH - (value / maxValue) * chartH;
                return { x: x, y: y };
            });

            // Área bajo la línea.
            ctx.beginPath();
            ctx.moveTo(points[0].x, pad.top + chartH);
            points.forEach(function (point) { ctx.lineTo(point.x, point.y); });
            ctx.lineTo(points[points.length - 1].x, pad.top + chartH);
            ctx.closePath();
            const gradient = ctx.createLinearGradient(0, pad.top, 0, pad.top + chartH);
            gradient.addColorStop(0, 'rgba(255,106,0,0.16)');
            gradient.addColorStop(1, 'rgba(255,106,0,0.02)');
            ctx.fillStyle = gradient;
            ctx.fill();

            ctx.beginPath();
            points.forEach(function (point, index) {
                if (index === 0) ctx.moveTo(point.x, point.y);
                else ctx.lineTo(point.x, point.y);
            });
            ctx.strokeStyle = '#ff6a00';
            ctx.lineWidth = 3;
            ctx.lineJoin = 'round';
            ctx.lineCap = 'round';
            ctx.stroke();

            points.forEach(function (point, index) {
                ctx.fillStyle = '#fff';
                ctx.beginPath();
                ctx.arc(point.x, point.y, 4, 0, Math.PI * 2);
                ctx.fill();
                ctx.strokeStyle = '#ff6a00';
                ctx.lineWidth = 2;
                ctx.stroke();

                ctx.fillStyle = '#9aa5b5';
                ctx.font = '12px Segoe UI, Arial';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'top';
                ctx.fillText(labels[index] || '', point.x, pad.top + chartH + 13);
            });
        }

        drawMonthlyChart();
        window.addEventListener('resize', drawMonthlyChart);
    }

    // Gráfico de categorías con SVG, también sin dependencias externas.
    const categoryChart = document.getElementById('categoryChart');
    if (categoryChart) {
        let labels = [];
        let values = [];
        try {
            labels = JSON.parse(categoryChart.dataset.labels || '[]');
            values = JSON.parse(categoryChart.dataset.values || '[]').map(Number);
        } catch (e) {
            labels = [];
            values = [];
        }

        const colors = ['#ff6a00', '#ff8c3a', '#ffad63', '#ffd29a', '#cdd2d9', '#ff7b22', '#f4a45d', '#e5b87d'];
        const rawTotal = values.reduce(function (sum, value) { return sum + value; }, 0);
        const total = rawTotal || 1;
        const size = 260;
        const cx = 130, cy = 130, radius = 82, stroke = 34;
        const circumference = 2 * Math.PI * radius;

        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 260 260');
        const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        group.setAttribute('transform', 'rotate(-90 130 130)');

        const base = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        base.setAttribute('cx', cx); base.setAttribute('cy', cy); base.setAttribute('r', radius);
        base.setAttribute('fill', 'none'); base.setAttribute('stroke', '#edf0f3'); base.setAttribute('stroke-width', stroke);
        group.appendChild(base);

        let offset = 0;
        values.forEach(function (value, index) {
            if (value <= 0) return;
            const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            const length = circumference * value / total;
            circle.setAttribute('cx', cx); circle.setAttribute('cy', cy); circle.setAttribute('r', radius);
            circle.setAttribute('fill', 'none');
            circle.setAttribute('stroke', colors[index % colors.length]);
            circle.setAttribute('stroke-width', stroke);
            circle.setAttribute('stroke-dasharray', length + ' ' + (circumference - length));
            circle.setAttribute('stroke-dashoffset', -offset);
            circle.setAttribute('stroke-linecap', 'butt');
            group.appendChild(circle);
            offset += length;
        });
        svg.appendChild(group);

        const center = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        const centerText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        centerText.setAttribute('x', '130'); centerText.setAttribute('y', '126'); centerText.setAttribute('text-anchor', 'middle');
        centerText.setAttribute('font-size', '24'); centerText.setAttribute('font-weight', '700'); centerText.setAttribute('fill', '#17243a');
        centerText.textContent = String(rawTotal);
        const centerSub = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        centerSub.setAttribute('x', '130'); centerSub.setAttribute('y', '147'); centerSub.setAttribute('text-anchor', 'middle');
        centerSub.setAttribute('font-size', '11'); centerSub.setAttribute('fill', '#95a0b0');
        centerSub.textContent = 'productos';
        center.appendChild(centerText); center.appendChild(centerSub); svg.appendChild(center);
        categoryChart.appendChild(svg);

        const legend = document.createElement('div');
        legend.className = 'donut-legend';
        labels.forEach(function (label, index) {
            const item = document.createElement('span');
            item.className = 'legend-item';
            item.innerHTML = '<span class="legend-dot" style="background:' + colors[index % colors.length] + '"></span>';
            const text = document.createElement('span');
            text.textContent = label;
            item.appendChild(text);
            legend.appendChild(item);
        });
        categoryChart.appendChild(legend);
    }
})();
