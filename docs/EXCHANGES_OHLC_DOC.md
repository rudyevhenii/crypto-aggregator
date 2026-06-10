# 📋 Документація API: Історичні дані OHLC (Клейни/Свічки)

Цей документ містить детальні технічні специфікації для інтеграції історичних фінансових свічок (OHLCV) з бірж **Binance
**, **Coinbase Exchange** та **Kraken**. Документ розроблено як інженерний довідник для розробників Java backend (Spring
WebFlux / Reactive stack).

---

## ⏱️ Шпаргалка: Одиниці виміру часу та формати

| Біржа        | Час у запиті (Параметри)      | Інтервал в запиті                | Час у відповіді (JSON)   | Порядок масиву (За замовчуванням) |
|:-------------|:------------------------------|:---------------------------------|:-------------------------|:----------------------------------|
| **Binance**  | **Мілісекунди** (`Long`)      | Строковий код (`1m`, `1h`, `1d`) | **Мілісекунди** (`Long`) | Хронологічний (Ascending)         |
| **Coinbase** | **ISO 8601 Рядок** (`String`) | Секунди (`Long`)                 | **Секунди** (`Long`)     | Зворотний (Descending) ⚠️         |
| **Kraken**   | **Секунди** (`Long`)          | Хвилини (`Integer`)              | **Секунди** (`Long`)     | Хронологічний (Ascending)         |

---

## 1. 🟡 Binance API

Найбільш передбачуване та розробнико-орієнтоване API. Усі часові обчислення відбуваються виключно в мілісекундах (
`epochMilli`).

* **Базовий URL:** `https://api.binance.com`
* **Ендпоінт:** `/api/v3/klines`
* **Патерн Java URI:** `/api/v3/klines?symbol=%s&interval=%s&endTime=%d&limit=%d`

### ⚙️ Параметри запиту

* `symbol` (`String`): Код торгової пари у верхньому регістрі без роздільників (наприклад, `BTCUSDT`, `ETHUSDT`).
* `interval` (`String`): Строковий код таймфрейму (наприклад, `1m`, `5m`, `15m`, `1h`, `4h`, `1d`, `1w`).
* `endTime` (`Long`): Верхня межа часу в **мілісекундах** (`cursor.toEpochMilli()`).
* `limit` (`Integer`): Кількість свічок для повернення (за замовчуванням 500, maximum 1000).

> 💡 **Архітектурна перевага Binance:** Передача параметрів `endTime` та `limit` позбавляє вас від необхідності
> розраховувати `startTime` на бекенді. Сервери Binance самостійно відраховують потрібну кількість інтервалів назад у
> минуле і повертають рівно `limit` свічок, де остання свічка закінчується вказаним `endTime`.

### 📦 Структура відповіді (JSON Array of Arrays)

Повертає позиційний масив масивів. Порядок елементів суворо зафіксований документацією:

```json5
[
  [
    1672531200000,
    // [0] Open Time (Мілісекунди, Long) -> Instant.ofEpochMilli(long)
    "63100.00",
    // [1] Open Price (Рядок -> new BigDecimal(String))
    "63500.00",
    // [2] High Price (Рядок -> new BigDecimal(String))
    "62900.00",
    // [3] Low Price (Рядок -> new BigDecimal(String))
    "63400.00",
    // [4] Close Price (Рядок -> new BigDecimal(String))
    "125.45300000",
    // [5] Volume (Рядок -> new BigDecimal(String))
    1672534799999,
    // [6] Close Time (Мілісекунди, Long) - Ігноруємо
    "7953723.12",
    // [7] Asset Volume (Ігноруємо)
    3084,
    // [8] Number of trades (Ігноруємо)
    "62.11",
    // [9] Taker buy base asset volume (Ігноруємо)
    "3923.4",
    // [10] Taker buy quote asset volume (Ігноруємо)
    "0"
    // [11] Unused field
  ]
]
```

---

## 2. 🔵 Coinbase Exchange API

Вимагає суворого дотримання формату ISO 8601 для дат. Має дві критичні особливості: межі запиту є включними (inclusive),
а дані повертаються у зворотному хронологічному порядку.

* **Базовий URL:** `https://api.exchange.coinbase.com` *(Увага: Не використовувати роздрібний домен api.coinbase.com!)*
* **Ендпоінт:** `/products/{product_id}/candles`
* **Патерн Java URI:** `/products/%s/candles?granularity=%d&start=%s&end=%s`

### ⚙️ Параметри запиту

* `product_id` (`String`): Код продукту з дефісом (наприклад, `BTC-USD`, `ETH-EUR`).
* `granularity` (`Long`): Інтервал свічки, виражений суворо у **секундах** (наприклад, `60` для 1m, `300` для 5m, `3600`
  для 1h, `86400` для 1d).
* `start` (`String`): ISO 8601 рядок початку вибірки (`startTime.toString()`, приклад: `2026-06-10T12:00:00Z`).
* `end` (`String`): ISO 8601 рядок кінця вибірки (`cursor.toString()`, приклад: `2026-06-10T14:00:00Z`).

### ⚠️ Критичні пастки розробника (Coinbase Overlap & Sorting)

1. **Ефект Дублювання:** Оскільки межі `start` та `end` є включними, якщо відправляти `cursor` у чистому вигляді,
   Coinbase поверне свічку, яка вже є на фронтенді.
    * **Рішення:** Зміщуйте курсор на 1 мілісекунду назад перед обчисленнями:
      `Instant cursor = request.getCursor().minusMillis(1);`.
2. **Проблема Паркану:** Формула розрахунку `startTime` не повинна містити декремент (`limit - 1`), оскільки зміщення
   курсора на 1 мс вже відрізало верхній "стовп паркану". Формула:
   `long startTime = cursor.getEpochSecond() - (intervalDuration.getSeconds() * request.getLimit());`.
3. **Зворотний порядок:** Найновіша свічка йде на початку відповіді. Для сумісності з бібліотеками графіків (наприклад,
   TradingView) масив потрібно обов'язково розвернути у хронологічний порядок.
    * **Рішення в Java Stream:** `.sorted(Comparator.comparing(HistoricalPriceDto::openTime))` перед термінальним
      оператором `.toList()`.

### Структура відповіді (JSON)

Повертає масив масивів. Увага: Найновіші свічки йдуть на початку масиву (Індекс [0] — це найсвіжіша свічка). Потрібне
примусове сортування у Java Stream!

```json5
[
  [
    1781179200,
    // 0: Open Time (Секунди, Long) -> Потрібно Instant.ofEpochSecond()
    61082.33,
    // 1: Low Price (Number -> BigDecimal)
    62568.00,
    // 2: High Price (Number -> BigDecimal)
    62262.01,
    // 3: Open Price (Number -> BigDecimal)
    61881.79,
    // 4: Close Price (Number -> BigDecimal)
    1918.66864234
    // 5: Volume (Number -> BigDecimal)
  ]
]
```

---

## 3. ⚪ Kraken API

Найбільш специфічне API для пагінації. Сервер Kraken **не підтримує параметр `endTime`**. Він завжди повертає дані від
точки `since` і до теперішнього моменту (або максимум 720 свічок).

* **Базовий URL:** `https://api.kraken.com`
* **Ендпоінт:** `/0/public/OHLC`
* **Патерн Java URI:** `/0/public/OHLC?pair=%s&interval=%d&since=%d`

### ⚙️ Параметри запиту

* `pair` (`String`): Назва торгової пари у форматі Kraken (наприклад, `XBTUSD`, `ETHUSD`).
* `interval` (`Integer`): Тривалість свічки в **хвилинах** (наприклад, `1` для 1m, `5` для 5m, `60` для 1h, `1440` для
  1d).
* `since` (`Long`): Unix Timestamp початку вибірки в **секундах** (`startTime.getEpochSecond()`).

### Структура відповіді (JSON)

Повертає складний об'єкт, де дані загорнуті в динамічний ключ з назвою пари, а значення полів свічки є сумішшю чисел та
рядків:

```json5
{
  "error": [],
  "result": {
    "XXBTZUSD": [
      // Динамічний ключ (назва пари)
      [
        1672531200,
        // 0: Open Time (Секунди, Long) -> Потрібно Instant.ofEpochSecond()
        "63100.0",
        // 1: Open Price (String -> BigDecimal)
        "63500.0",
        // 2: High Price (String -> BigDecimal)
        "62900.0",
        // 3: Low Price (String -> BigDecimal)
        "63400.0",
        // 4: Close Price (String -> BigDecimal)
        "63250.5",
        // 5: Vwap Price (Ігноруємо)
        "125.453",
        // 6: Volume (String -> BigDecimal)
        432
        // 7: Count (Кількість угод, Ігноруємо)
      ]
    ],
    "last": 1672534800
    // Timestamp останньої свічки (Секунди)
  }
}
```
