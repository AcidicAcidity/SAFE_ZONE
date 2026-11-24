FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_ENABLE_RENTAL_CREATION=false
ARG VITE_ENABLE_USER_STATUS_UPDATES=false
ARG VITE_ENABLE_RENTAL_STATUS_UPDATES=false
ENV VITE_ENABLE_RENTAL_CREATION=$VITE_ENABLE_RENTAL_CREATION \
    VITE_ENABLE_USER_STATUS_UPDATES=$VITE_ENABLE_USER_STATUS_UPDATES \
    VITE_ENABLE_RENTAL_STATUS_UPDATES=$VITE_ENABLE_RENTAL_STATUS_UPDATES
RUN npm run build

FROM nginx:stable-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1/healthz || exit 1
