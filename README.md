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

## Где находится код
- Основной вход плагина: `src/main/java/com/example/vocabplugin/VocabSignPlugin.java`
- Команды: `src/main/java/com/example/vocabplugin/command/VocabCommand.java`
- GUI и обработчики интерфейса:
  - `src/main/java/com/example/vocabplugin/VocabularyGuiFactory.java`
  - `src/main/java/com/example/vocabplugin/VocabularyGuiListener.java`
  - `src/main/java/com/example/vocabplugin/VocabularyGuiHolder.java`
- Работа с хранилищами:
  - `src/main/java/com/example/vocabplugin/storage/YamlVocabularyStorage.java`
  - `src/main/java/com/example/vocabplugin/storage/MySqlVocabularyStorage.java`
- Переводы:
  - `src/main/java/com/example/vocabplugin/translation/TranslationService.java`
  - `src/main/java/com/example/vocabplugin/translation/GoogleTranslationProvider.java`
  - `src/main/java/com/example/vocabplugin/translation/LocalDictionaryTranslationProvider.java`
  - `src/main/java/com/example/vocabplugin/translation/OverrideTranslationProvider.java`

## Как выложить этот код на GitHub
Если репозиторий ещё не связан с GitHub, выполните в корне проекта:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<your_user>/<your_repo>.git
git push -u origin main
```

Если удалённый репозиторий уже есть, проверьте его:

```bash
git remote -v
git push
```
