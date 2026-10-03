package handler

import (
	"crypto/hmac"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"telkomsecure-backend/internal/model"
)

var jwtSecret = []byte("telkomsecure_soc_jwt_super_secret_key_2026_prod")

type OperatorClaims struct {
	ID        string    `json:"id"`
	Email     string    `json:"email"`
	Name      string    `json:"name"`
	Role      string    `json:"role"`
	ExpiresAt time.Time `json:"expires_at"`
}

func GenerateToken(op *model.Operator, duration time.Duration) (string, time.Time, error) {
	exp := time.Now().Add(duration)
	claims := OperatorClaims{
		ID:        op.ID,
		Email:     op.Email,
		Name:      op.Name,
		Role:      op.Role,
		ExpiresAt: exp,
	}

	headerJSON := `{"alg":"HS256","typ":"JWT"}`
	headerB64 := base64.RawURLEncoding.EncodeToString([]byte(headerJSON))

	payloadBytes, err := json.Marshal(claims)
	if err != nil {
		return "", time.Time{}, err
	}
	payloadB64 := base64.RawURLEncoding.EncodeToString(payloadBytes)

	data := headerB64 + "." + payloadB64
	mac := hmac.New(sha256.New, jwtSecret)
	mac.Write([]byte(data))
	sigB64 := base64.RawURLEncoding.EncodeToString(mac.Sum(nil))

	return data + "." + sigB64, exp, nil
}

func ValidateToken(tokenStr string) (*OperatorClaims, error) {
	parts := strings.Split(tokenStr, ".")
	if len(parts) != 3 {
		return nil, fmt.Errorf("format token tidak valid")
	}

	data := parts[0] + "." + parts[1]
	mac := hmac.New(sha256.New, jwtSecret)
	mac.Write([]byte(data))
	expectedSig := base64.RawURLEncoding.EncodeToString(mac.Sum(nil))

	if !hmac.Equal([]byte(parts[2]), []byte(expectedSig)) {
		return nil, fmt.Errorf("tanda tangan (signature) token tidak valid")
	}

	payloadBytes, err := base64.RawURLEncoding.DecodeString(parts[1])
	if err != nil {
		return nil, fmt.Errorf("gagal decode payload token: %v", err)
	}

	var claims OperatorClaims
	if err := json.Unmarshal(payloadBytes, &claims); err != nil {
		return nil, fmt.Errorf("format klaim token tidak valid")
	}

	if time.Now().After(claims.ExpiresAt) {
		return nil, fmt.Errorf("sesi login telah kedaluwarsa, silakan login ulang")
	}

	return &claims, nil
}
