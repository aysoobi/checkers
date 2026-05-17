# Checkers —  шашки

Веб-платформа для игры в ** шашки** : ИИ, история партий, регистрация, статистика побед/поражений, темы, подсказки, адаптивный интерфейс.

## Для кого и зачем

- Тренировка шашек в браузере без установки приложения.
- Прогресс сохраняется: аккаунт, история партий, счётчик побед и поражений.
- Удобно с телефона и компьютера.

## Стек

- **Backend:** Java 17, Spring Boot 3.2, Spring Security, JPA
- **БД:** H2 на диске (по умолчанию, данные не пропадают) или PostgreSQL (prod / Docker)
- **Frontend:** HTML, CSS, JavaScript

## Запуск

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)   # macOS, JDK 17
cd incubator
./gradlew bootRun
```

Откройте **http://localhost:8082**

1. **Регистрация** → `/register.html` (логин + пароль)
2. **Вход** → `/login.html`
3. Играйте против ИИ или вдвоём, смотрите историю и статистику

После **перезапуска** `./gradlew bootRun` все аккаунты и история **остаются** — база в папке `data/checkers.mv.db`.

### PostgreSQL (как Supabase / облачная БД)

```bash
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=prod'
```


Локальная проверка образа:

```bash
docker build -t checkers:latest .
docker run --rm -p 8080:8080 \
  -e PGHOST=host.docker.internal -e PGPORT=5432 \
  -e PGUSER=postgres -e PGPASSWORD=postgres -e PGDATABASE=checkers \
  checkers:latest
```

## Сохранение данных

| Данные | Где |
|--------|-----|
| Логин | `profiles.username` |
| Пароль | `profiles.password_hash` (BCrypt, не в открытом виде) |
| Победы / поражения | `profiles.wins`, `losses`, `draws` |
| История партий | таблица `games` (ходы в JSON) |

Файл БД (режим по умолчанию): `./data/checkers.mv.db`

## API

| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/api/auth/register` | Регистрация |
| POST | `/api/auth/login` | Вход |
| GET | `/api/auth/me` | Текущий пользователь + статистика |
| POST | `/api/game/new` | Новая игра |
| GET | `/api/game/{id}/hints` | Подсказки |
| GET | `/api/history` | История партий |

Сессия — cookie (после входа браузер помнит пользователя).

## Чеклист

- [x] ИИ: EASY / MEDIUM / HARD (minimax)
- [x] История партий в БД
- [x] Регистрация и вход, сохранение прогресса
- [x] Счётчик побед / поражений
- [x] Светлая и тёмная тема
- [x] Подсказки ходов
- [x] Адаптивная вёрстка
- [x] Постоянная БД (переживает перезапуск)

## Сборка и тесты

```bash
./gradlew clean build
```
