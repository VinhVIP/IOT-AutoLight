#include "LiquidCrystal_I2C.h"
#include "DHT.h"
#include <Wire.h>
#include <SoftwareSerial.h>
#include "Thread.h"
#include "StaticThreadController.h"
#include "BH1750.h"


const int LED_PIN = 9;

// ---------- Bluetooth Module -------------------
const int TX_PIN = 2;
const int RX_PIN = 3;

// ---------- Cảm biến siêu âm HC-SR04 -------------------
const int TRIG_PIN = 8;
const int ECHO_PIN = 7;

// ---------- Cảm biến nhiệt độ, độ ẩm -------------------
const int DHTPIN = 4;
const int DHTTYPE = DHT11;



// ---------- Các hằng số ----------------------
const int DELAY_TIME = 1000;  // Thời gian delay cảm biến siêu âm (vật cản)

const int INIT = 1;

const int LED_TURN_OFF = 20;
const int LED_TURN_ON = 21;

const int LED_AUTO_LIGHT_ON = 22;
const int LED_AUTO_LIGHT_OFF = 23;

const int SEND_TEMP = 10;
const int SEND_HUMI = 11;
const int SEND_LIGHT = 12;


// -------------- Variable ----------------------
bool hold = false, isLightOn = false, isAutoLight = true;

int cmd = 0;          // Lệnh được gửi từ Smartphone
int brightness = 10;  // Cường độ sáng của đèn led

int temperature = 0;  // Giá trị nhiệt độ
int humidity = 0;     // Giá trị độ ẩm
int lux = 0;          // Giá trị cường độ ánh sáng

long time = 0;
bool check, checkBef = false;

int maxLux = 600, minBri = 30, maxBri = 80, maxDistance = 30;

byte degree[8] = {0B01110, 0B01010, 0B01110, 0B00000, 0B00000, 0B00000, 0B00000, 0B00000};


BH1750 lightMeter;
SoftwareSerial HC05(TX_PIN, RX_PIN);
DHT dht(DHTPIN, DHTTYPE);
LiquidCrystal_I2C lcd(0x27, 16, 2);



void distanceCallback() {
  long duration, distanceCm;

  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  duration = pulseIn(ECHO_PIN, HIGH);

  distanceCm = duration / 29.1 / 2;

  if (distanceCm > maxDistance) {
    time = 0;
    check = checkBef = false;
  } else {
    Serial.println(time);
    check = true;

    if (millis() - time >= DELAY_TIME && !checkBef) {
      isLightOn = !isLightOn;
      HC05.write(isLightOn ? LED_TURN_ON : LED_TURN_OFF);

      checkBef = check;
    }
  }
}

void dhtCallback() {
  int l = lightMeter.readLightLevel();

  lcd.setCursor(7, 1);
  lcd.print("        ");
  lcd.setCursor(7, 1);
  lcd.print(l);
  lcd.print(" lux");


  int h = dht.readHumidity();
  int t = dht.readTemperature();

  lcd.setCursor(11, 0);
  lcd.print(t);
  lcd.write(1);
  lcd.print("C");

  lcd.setCursor(1, 1);
  lcd.print(h);
  lcd.print("%");

  if (l != lux) {
    lux = l;
    sendLuxValue(lux);
  }

  if (t != temperature) {
    temperature = t;
    HC05.write(SEND_TEMP);
    HC05.write(t);
  }
  if (h != humidity) {
    humidity = h;
    HC05.write(SEND_HUMI);
    HC05.write(h);
  }

  if (cmd == INIT) {
    temperature = t;
    HC05.write(SEND_TEMP);
    HC05.write(t);

    humidity = h;
    HC05.write(SEND_HUMI);
    HC05.write(h);

    lux = l;
    sendLuxValue(lux);

    cmd = -1;
  }
}

void sendLuxValue(int lux) {
  HC05.write(SEND_LIGHT);
  HC05.write(lux / 1000);
  HC05.write(lux % 1000);
}


void ledCallback() {
  if (isLightOn) {
    if (isAutoLight) {
      if (lux <= maxLux) {
        int value = maxBri - lux / (maxLux / (maxBri - minBri));
        ledBrightness(value);
      } else {
        isLightOn = !isLightOn;
        HC05.write(isLightOn ? LED_TURN_ON : LED_TURN_OFF);
      }
    } else {
      ledBrightness(brightness);
    }
  } else {
    ledBrightness(0);
  }
}

void bluetoothCallback() {
  if (HC05.available()) {
    if (cmd == LED_AUTO_LIGHT_OFF) {
      brightness = HC05.read();
      // Debug
      Serial.print("brightness: ");
      Serial.println(brightness);

      cmd = 0;
    } else {
      cmd = HC05.read();

      // Debug
      Serial.print("Command: ");
      Serial.println(cmd);


      if (cmd == LED_TURN_OFF) {
        isLightOn = false;
      } else if (cmd == LED_TURN_ON) {
        isLightOn = true;
      } else if (cmd == LED_AUTO_LIGHT_OFF) {
        isAutoLight = false;
      } else if (cmd == LED_AUTO_LIGHT_ON) {
        isAutoLight = true;
      }
    }

  }
}

Thread* distanceThread = new Thread(distanceCallback);
Thread* dhtThread = new Thread(dhtCallback);
Thread* ledThread = new Thread(ledCallback);
Thread* bluetoothThread = new Thread(bluetoothCallback);

StaticThreadController<4> controller (distanceThread, dhtThread, ledThread, bluetoothThread);


// ------- setup -------
void setup()
{
  Serial.begin(9600);     // giao tiếp Serial với baudrate 9600

  Wire.begin();
  lightMeter.begin();

  pinMode(TRIG_PIN, OUTPUT);  // chân trig sẽ phát tín hiệu
  pinMode(ECHO_PIN, INPUT);   // chân echo sẽ nhận tín hiệu

  lcd.init();
  lcd.backlight();

  lcd.setCursor(1, 0);
  lcd.print("Nhiet do: ");


  lcd.createChar(1, degree);

  HC05.begin(9600);
  pinMode(RX_PIN, OUTPUT);
  pinMode(TX_PIN, INPUT);


  distanceThread->setInterval(100);
  dhtThread->setInterval(1000);
  ledThread->setInterval(10);
  bluetoothThread->setInterval(10);
}

// ------- loop -------
void loop() {
  controller.run();
  delay(300);
}

void ledBrightness(int brightness) {
  analogWrite(LED_PIN, 255 * (brightness * 1.0 / 100));
}
