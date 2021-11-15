# PGR203 Avansert Java eksamen

[![ExamBuild](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/maven.yml)

### Arbeidsform:
* Utvikling av dette eksamensprosjektet benyttet vi oss av verktøy som "Code With Me". Det er en plugin som brukes via Intelliji, slik at
begge parter kan kode live. 

### Ekstra leveranse:
* Programmet kan slette eksisterende spørreundersøkelse, spørsmål og alternativ. Dersom en spørreundersøkelse blir slettet, slettes alt som
følger med også (tilhørende spørsmål + alternativ). Dette gjelder ved sletting av spørsmål også. 

### Bygging av programmet
* Dette programmet gir brukeren mulighet til å opprette og svare på spørreundersøkelser. For å kjøre programmet kan man enten bruke main() i SurveyManager- klassen eller kjøre via mvn package.
For å kunne kjøre via mvn package må brukeren trykke på tannhjul hvor det står `package` (eventuelt `clean` først dersom `target`-mappen ikke er tom). *Deretter kan brukeren skrive inn denne kommandoen på terminal: 
`java -jar target/pgr203-exam-Neutix-1.0-SNAPSHOT.jar` eller `java -Dfile.encoding=utf-8 -jar target/pgr203-exam-Neutix-1.0-SNAPSHOT.jar` for riktig encoding via jar. 

### Funksjonalitet/ beskrivelse
* På forsiden har brukeren flere valg: opprette, redigere eksisterende, svare eller se alle svar.

## Vedlegg: Sjekkliste for innlevering

* [ ] Dere har lest eksamensteksten
* [ ] Dere har lastet opp en ZIP-fil med navn basert på navnet på deres Github repository

### README.md

* [x] `README.md` inneholder en korrekt link til Github Actions
* [ ] `README.md` beskriver prosjektets funksjonalitet, hvordan man bygger det og hvordan man kjører det
* [x] `README.md` beskriver eventuell ekstra leveranse utover minimum
* [ ] `README.md` inneholder et diagram som viser datamodellen

