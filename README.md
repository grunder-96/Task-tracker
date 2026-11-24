# Task tracker

## Локальный запуск
1. Изменить название файла `.env.example` на `.env` и заполнить значения.

2. Поднять инфраструктуру (Postgres, Kafka):
   ```bash
   docker compose up -d
   ```
4. Запустить сервисы `task-tracker-backend`, `task-tracker-email-sender`, \
`task-tracker-summarization-server` и `task-tracker-scheduler` с active profile **local**.

6. Приступить к работе с функционалом приложения:
   - Frontend: `http://localhost` (port 8080)

## Production

### Важно!

В данном случае будет описана ситуация, когда Caddy не может получить сертификат 
из-за блокировок на стороне хостинга. 

### Требования

- VPS с Ubuntu/Debian и root-доступом.
- Домен (или поддомен), указывающий на IP сервера:
- Открытые порты 80 и 443.

### 1. Подготовка сервера

```bash
apt update && apt upgrade -y

# Установка Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker $USER

# Переподключиться по SSH, чтобы группа применилась
```

### 2. Настройка firewall

```bash
ufw allow OpenSSH
ufw allow 80
ufw allow 443
ufw enable
ufw status verbose
```

> **Важно:** `ufw allow OpenSSH` должен быть выполнен **до** `ufw enable`, иначе доступ к серверу будет потерян.

### 3. Клонирование репозитория

```bash
git clone <repo-url> ~/Task-tracker
cd ~/Task-tracker
```

### 4. Создание `.env.prod`
```bash
nano .env.prod
```

Пример содержимого:

```env
DB_NAME:
DB_USER:
DB_PASSWORD:
SUMMARIZATION_DB_NAME:
SUMMARIZATION_DB_USER:
SUMMARIZATION_DB_PASSWORD:

JWT_SECRET:example - openssl rand -base64 32

KAFKA_EMAIL_SENDING_TOPIC:
KAFKA_SUMMARIZATION_REQUESTS_TOPIC:
KAFKA_SUMMARIZATION_RESPONSES_TOPIC:

SENDER_SERVER_HOST=
SENDER_SERVER_PORT=
SENDER_SERVER_LOGIN=
SENDER_SERVER_PASSWORD=
SENDER_FROM_EMAIL=
SENDER_SMTP_STARTTLS=
SENDER_SMTP_AUTH=

LLM_AUTH_KEY=
LLM_AUTH_URL=
LLM_API_BASE_URL
LLM_MODEL_NAME=
```
### 5. Создание `Caddyfile`

В той же папке, что и `docker-compose-prod.yaml`:

```bash
nano Caddyfile
```

```caddy
your-domain.com {
    tls /certs/pet-project-for.fun.crt /certs/pet-project-for.fun.key
    reverse_proxy backend:80
}
```

Caddy не сможет автоматически получит TLS-сертификаты от Let's Encrypt, поэтому придется сделать это самостоятельно.

### 6. Получение сертификатов

Выполните на вашем VPS (вне Docker, на хосте):

```bash
# Установка certbot (для Fedora)
sudo dnf install certbot

# Запуск выпуска сертификата через DNS-проверку
sudo certbot certonly --manual --preferred-challenges dns -d your-domain.com
```
Certbot попросит вас добавить TXT-запись в DNS вашего домена. Вам нужно зайти в панель управления 
DNS вашего регистратора и создать TXT-запись с именем _acme-challenge и значением, которое выдал Certbot. 
Certbot проверит запись и, если всё верно, сохранит сертификат на диск.

### 7. Подготовка сертификатов для Caddy

Создайте папку `certs` рядом с вашим docker-compose-prod.yaml и скопируйте туда файлы, переименовав их для удобства:

```bash
mkdir -p ./certs

sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem ./certs/your-domain.com.crt
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem ./certs/your-domain.com.key

sudo chmod 644 ./certs/your-domain.com.crt
sudo chmod 644 ./certs/your-domain.com.key
```

Укажите Caddy использовать ваши файлы вместо автоматического получения:

```Caddyfile
your-domain.com {
    tls /certs/your-domain.com.crt /certs/your-domain.com.key
    reverse_proxy backend:8080
}
```
Каталог `certs` уже примонтирован в docker-compose-prod.yaml.

### 8. Запуск

```bash
docker compose --env-file .env.prod -f docker-compose-prod.yaml up -d
```

### 9. Проверка
Открыть в браузере:
- `https://your-domain.com`
