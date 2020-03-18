#include <ESP8266WiFi.h>        // Include the Wi-Fi library

const char* ssid     = "FaceLock";         // The SSID (name) of the Wi-Fi network you want to connect to
const char* password = "";     // The password of the Wi-Fi network


#define LOCK_PIN D3

#include <Servo.h>
#include <ESP8266WiFi.h>
#include "./DNSServer.h"                  // Patched lib
#include <ESP8266WebServer.h>

const byte        DNS_PORT = 53;          // Capture DNS requests on port 53
IPAddress         apIP(10, 10, 10, 1);    // Private network for server
DNSServer         dnsServer;              // Create the DNS object
ESP8266WebServer  webServer(80);          // HTTP server
Servo name_servo;
int pos = 0;

void unlock(){
  Serial.println("UNLOCKING!"); //180 degrees turn left
  name_servo.writeMicroseconds(1700);
  delay(2350); //play with this value
  name_servo.writeMicroseconds(1500);
  webServer.send(200, "text/plain", "unlocked successfully");
}

void lock(){
  Serial.println("LOCKING!"); //180 degrees turn right
  name_servo.writeMicroseconds(1300);
  delay(2500); //play with this value, should be a little bit higher than the "unlock" value
  name_servo.writeMicroseconds(1500);
  webServer.send(200, "text/plain", "locked successfully");
}

void setup() { 
 Serial.begin(115200);
 WiFi.begin(ssid, password);             // Connect to the network
  Serial.print("Connecting to ");
  Serial.print(ssid); Serial.println(" ...");

  int i = 0;
  while (WiFi.status() != WL_CONNECTED) { // Wait for the Wi-Fi to connect
    delay(1000);
    Serial.print(++i); Serial.print(' ');
  }
  Serial.println("Connection established!");  
  Serial.print("IP address:\t");
  Serial.println(WiFi.localIP());         // Send the IP address of the ESP8266 to the computer
  name_servo.attach (LOCK_PIN);

 Serial.setDebugOutput(true);
  // if DNSServer is started with "*" for domain name, it will reply with
  // provided IP to all DNS request
  dnsServer.start(DNS_PORT, "*", apIP);
  webServer.onNotFound([]() {
    webServer.send(200, "text/html", "Face Lock Connected!");
  });
  webServer.on("/lock", lock);
  webServer.on("/unlock", unlock);

  webServer.begin();
} 

void loop() { 
  dnsServer.processNextRequest();
  webServer.handleClient();
} 
