# Multi-stage Dockerfile for TelkomSecure Backend Engine
# Stage 1: Builder
FROM golang:1.24-alpine AS builder

WORKDIR /app

# Copy dependency manifests
COPY go.mod ./
RUN go mod download

# Copy source code
COPY cmd/ ./cmd/
COPY internal/ ./internal/

# Compile statically-linked Go binary
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /app/server ./cmd/server

# Stage 2: Minimal Production Image
FROM alpine:3.20

WORKDIR /app

# Install certificates and timezone data
RUN apk --no-cache add ca-certificates tzdata

# Copy compiled binary
COPY --from=builder /app/server /app/server

# Copy initial data store seed
COPY data_store.json /app/data_store.json

# Runtime environment
ENV PORT=8080
ENV PERSIST_FILE=/app/data_store.json

EXPOSE 8080

CMD ["/app/server"]
