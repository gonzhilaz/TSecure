package handler

import (
	"encoding/json"
	"fmt"
	"net/http"
	"sync"
)

// EventBroker coordinates Server-Sent Events (SSE) connections to dashboards.
type EventBroker struct {
	mu           sync.Mutex
	clients      map[chan string]bool
	newClients   chan chan string
	closeClients chan chan string
	messages     chan string
}

// NewEventBroker initializes a new SSE broker.
func NewEventBroker() *EventBroker {
	b := &EventBroker{
		clients:      make(map[chan string]bool),
		newClients:   make(chan chan string),
		closeClients: make(chan chan string),
		messages:     make(chan string, 100),
	}
	go b.listen()
	return b
}

func (b *EventBroker) listen() {
	for {
		select {
		case s := <-b.newClients:
			b.mu.Lock()
			b.clients[s] = true
			b.mu.Unlock()
		case s := <-b.closeClients:
			b.mu.Lock()
			delete(b.clients, s)
			close(s)
			b.mu.Unlock()
		case msg := <-b.messages:
			b.mu.Lock()
			for s := range b.clients {
				select {
				case s <- msg:
				default:
				}
			}
			b.mu.Unlock()
		}
	}
}

// Broadcast sends an event payload to all active web dashboard sessions.
func (b *EventBroker) Broadcast(eventType string, data any) {
	bytes, err := json.Marshal(data)
	if err != nil {
		return
	}
	msg := fmt.Sprintf("event: %s\ndata: %s\n\n", eventType, string(bytes))
	b.messages <- msg
}

// ServeHTTP streams SSE events to connecting clients.
func (b *EventBroker) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	flusher, ok := w.(http.Flusher)
	if !ok {
		http.Error(w, "Streaming unsupported", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "text/event-stream")
	w.Header().Set("Cache-Control", "no-cache")
	w.Header().Set("Connection", "keep-alive")
	w.Header().Set("Access-Control-Allow-Origin", "*")

	msgChan := make(chan string, 10)
	b.newClients <- msgChan

	defer func() {
		b.closeClients <- msgChan
	}()

	// Send initial ping
	fmt.Fprintf(w, "event: connected\ndata: {\"status\":\"connected\"}\n\n")
	flusher.Flush()

	notify := r.Context().Done()
	for {
		select {
		case <-notify:
			return
		case msg := <-msgChan:
			fmt.Fprint(w, msg)
			flusher.Flush()
		}
	}
}
