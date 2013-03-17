package main

import (
  "os"
  "fmt"
  "net/http"
  "time"
)

type Payload struct {
	time	int
	payload	string
}

var rendez map[string]

func home(w http.ResponseWriter, r *http.Request) {
  r.ParseForm()
  f := r.Form
  me := f['me'][0]
  peer := f['peer'][0]
  value := f['value'][0]
  now := time.Now().Unix()
  fmt.Fprintf(w, "Hello World! %d", now)

}

func main() {
  http.HandleFunc("/", home)
  http.ListenAndServe("0.0.0.0:30332", nil)
}
