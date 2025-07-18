FROM node:22-alpine AS frontend

COPY frontend/ /frontend

WORKDIR /frontend

RUN npm install
RUN npm run build


FROM nginx:1.27.4-alpine AS nginx

COPY --from=frontend /frontend/build/ /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

