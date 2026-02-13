# VocabSignPlugin (Paper 1.21.8)

Плагин: ПКМ по восковой табличке открывает GUI слов. Игрок может добавить валидные английские слова в свой словарь с переводом.

## Что нужно для сборки
- JDK 21
- Maven 3.9+
- Доступ к интернету для скачивания зависимостей Maven

## Сборка
```bash
mvn clean package
```

Готовый jar:
```text
target/vocab-sign-plugin-1.1.0.jar
```

## Установка на сервер
1. Скопировать `target/vocab-sign-plugin-1.1.0.jar` в папку `plugins/` вашего Paper-сервера.
2. Перезапустить сервер.
3. Настроить `plugins/VocabSignPlugin/config.yml`.

## Google Translate
По умолчанию выключен. Чтобы включить:
```yaml
translation:
  google:
    enabled: true
    api-key: "ВАШ_GOOGLE_API_KEY"
    source-language: "en"
    target-language: "ru"
```

Без ключа плагин продолжит работать через override + локальный fallback словарь.

## Хранилище
- YAML (по файлу на игрока)
- MySQL

В `config.yml`:
```yaml
storage:
  type: "yaml" # yaml | mysql
```

Для MySQL заполнить `host/port/database/username/password`.

## Команды
- `/vocab reload`
- `/vocab override set <word> <translation>`
- `/vocab override remove <word>`
- `/vocab override list`
- `/vocab export <player> [csv|json]`
