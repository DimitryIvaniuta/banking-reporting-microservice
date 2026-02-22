FROM python:3.12-slim
WORKDIR /app
COPY observability/modsec-exporter/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY observability/modsec-exporter/exporter.py ./exporter.py
EXPOSE 9910
CMD ["python", "/app/exporter.py"]
