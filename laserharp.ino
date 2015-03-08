/*
 * @author Andrej Budinčević <andrew@hotmail.rs>
 *
 * Laser Harp controller - Arduino C
 *
 */

/* Konstante */

const int digPin = 8; /* Id digitalnog pina koji se koristi za citanje podataka sa sedmog fotootpornika */
const int eps = 512; /* Ogranicenje, varijabilno (0-1023) */

boolean currPlaying[7]; /* Trenutno stanje pina */

void setup() {
	/* Pokreni serial output */
	Serial.begin(9600);
}

void loop() { 
	int val = 0;

	/* Analogni pinovi, obican analog read je dovoljan */

	for(int i = 0; i < 6; i++) {
		val = analogRead(i);

		if((val < eps) && !currPlaying[i]) 
			sendCommand(i, 1, val); /* Snop je prekinut, pusti notu ukoliko vec nije pustena (1) */

		if((val >= eps) && currPlaying[i]) 
			sendCommand(i, 0, val); /* Snop udara u fotootpornik, zaustavi notu (0) */
	}

	/* Sedma nota na digitalnom pinu */
	/* Poor man's A/D converter */

	val = 0;

	pinMode(digPin, OUTPUT);
	digitalWrite(digPin, LOW);

	delay(10);

	pinMode(digPin, INPUT);
	while(digitalRead(digPin) == LOW)
		val++; 

	if((val < eps) && !currPlaying[6]) 
		sendCommand(6, 1, val); /* Snop je prekinut, pusti notu ukoliko vec nije pustena (1) */

	if((val >= eps) && currPlaying[6]) 
		sendCommand(6, 0, val); /* Snop udara u fotootpornik, zaustavi notu (0) */
}

void sendCommand(int noteId, int com, int val) {
	/* Ispisuje info na serijski output koji Java program cita */
	Serial.print(noteId);
	Serial.println(com);

	currPlaying[noteId] = !currPlaying[noteId];

	/* Blinking LED, yay! */
	digitalWrite(13, HIGH);
	delay(100);
	digitalWrite(13, LOW);
}