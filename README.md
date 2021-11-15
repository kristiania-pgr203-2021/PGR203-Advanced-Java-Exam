# PGR203 Avansert Java eksamen

[![ExamBuild](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/maven.yml)

[![build](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/build.yml/badge.svg)](https://github.com/kristiania-pgr203-2021/pgr203-exam-Neutix/actions/workflows/build.yml)


## Beskriv hvordan programmet skal testes:

## Sjekkliste

### Arbeidsform:
Utvikling av dette eksamensprosjektet benyttet vi oss av verktøy som "Code With Me". Det er en plugin som brukes via Intelliji, slik at
begge parter kan kode live. 

### Ekstra leveranse:
* Programmet kan slette eksisterende spørreundersøkelse, spørsmål og alternativ. Dersom en spørreundersøkelse blir slettet, slettes alt som
følger med også (tilhørende spørsmål + alternativ). Dette gjelder ved sletting av spørsmål også. 

### Bygging av programmet
Dette programmet gir brukeren mulighet til å opprette og svare på spørreundersøkelser. For å kjøre programmet kan man enten bruke main() i SurveyManager-klassen eller kjøre via mvn package.
For å kunne kjøre via mvn package må brukeren trykke på tannhjul hvor det står `package` (eventuelt `clean` først dersom `target`-mappen ikke er tom). Deretter kan brukeren skrive inn denne kommandoen på terminal: 
`java -jar target/pgr203-exam-Neutix-1.0-SNAPSHOT.jar` eller `java -Dile.encoding=utf-8 -jar target/pgr203-exam-Neutix-1.0-SNAPSHOT.jar` for riktig encoding via jar. 

### Funksjonalitet/ beskrivelse
På forsiden har brukeren flere valg: opprette, redigere eksisterende, svare eller se alle svar.


## Vedlegg: Sjekkliste for innlevering

* [ ] Dere har lest eksamensteksten
* [ ] Dere har lastet opp en ZIP-fil med navn basert på navnet på deres Github repository
* [x] Koden er sjekket inn på github.com/pgr203-2021-repository
* [x] Dere har committed kode med begge prosjektdeltagernes GitHub konto (alternativt: README beskriver arbeidsform)

### README.md

* [x] `README.md` inneholder en korrekt link til Github Actions
* [ ] `README.md` beskriver prosjektets funksjonalitet, hvordan man bygger det og hvordan man kjører det
* [x] `README.md` beskriver eventuell ekstra leveranse utover minimum
* [ ] `README.md` inneholder et diagram som viser datamodellen

### Koden

* [x] `mvn package` bygger en executable jar-fil
* [x] Koden inneholder et godt sett med tester
* [x] `java -jar target/...jar` (etter `mvn package`) lar bruker legge til og liste ut data fra databasen via webgrensesnitt
* [x] Serveren leser HTML-filer fra JAR-filen slik at den ikke er avhengig av å kjøre i samme directory som kildekoden
* [x] Programmet leser `dataSource.url`, `dataSource.username` og `dataSource.password` fra `pgr203.properties` for å connecte til databasen
* [x] Programmet bruker Flywaydb for å sette opp databaseskjema
* [x] Server skriver nyttige loggmeldinger, inkludert informasjon om hvilken URL den kjører på ved oppstart

### Funksjonalitet

* [x] Programmet kan opprette spørsmål og lagrer disse i databasen (påkrevd for bestått)
* [x] Programmet kan vise spørsmål (påkrevd for D)
* [x] Programmet kan legge til alternativ for spørsmål (påkrevd for D)
* [x] Programmet kan registrere svar på spørsmål (påkrevd for C)
* [x] Programmet kan endre tittel og tekst på et spørsmål (påkrevd for B)

### Ekstraspørsmål (dere må løse mange/noen av disse for å oppnå A/B)

* [ ] !!!JDBC koden fra forelesningen har en feil ved retrieve dersom id ikke finnes. Kan dere rette denne?
* [ ] !!!Vi har sett på hvordan å bruke AbstractDao for å få felles kode for retrieve og list. Kan dere bruke felles kode i AbstractDao for å unngå duplisering av inserts og updates?
* [x] I forelesningen fikk vi en rar feil med CSS når vi hadde `<!DOCTYPE html>`. Grunnen til det er feil content-type. Klarer dere å fikse det slik at det fungerer å ha `<!DOCTYPE html>` på starten av alle HTML-filer?
* [x] Når brukeren utfører en POST hadde det vært fint å sende brukeren tilbake til dit de var før. Det krever at man svarer med response code 303 See other og headeren Location
* [x] Når brukeren skriver inn en tekst på norsk må man passe på å få encoding riktig. Klarer dere å lage en <form> med action=POST og encoding=UTF-8 som fungerer med norske tegn? Klarer dere å få æøå til å fungere i tester som gjør både POST og GET?
* [x] Dersom brukeren går til http://localhost:8080 får man 404. Serveren burde i stedet returnere innholdet av index.html
* [ ] !!!Før en bruker svarer på et spørsmål hadde det vært fint å la brukeren registrere navnet sitt. Klarer dere å implementere dette med cookies? Lag en form med en POST request der serveren sender tilbake Set-Cookie headeren. Browseren vil sende en Cookie header tilbake i alle requester. Bruk denne til å legge inn navnet på brukerens svar
* [ ] !!! Dersom noe alvorlig galt skjer vil serveren krasje. Serveren burde i stedet logge dette og returnere en status code 500 til brukeren
* [ ] !!! Et favorittikon er et lite ikon som nettleseren viser i tab-vinduer for en webapplikasjon. Kan dere lage et favorittikon for deres server? Tips: ikonet er en binærfil og ikke en tekst og det går derfor ikke an å laste den inn i en StringBuilder
* [x] Klarer dere å lage en Coverage-rapport med GitHub Actions med Coveralls? (Advarsel: Foreleser har nylig opplevd feil med Coveralls så det er ikke sikkert dere får det til å virke)
* [ ] Kan dere lage noen diagrammer som illustrerer hvordan programmet deres virker?
* [ ] Å opprette og liste spørsmål hadde vært logisk og REST-fult å gjøre med GET /api/questions og POST /api/questions. Klarer dere å endre måten dere hånderer controllers på slik at en GET og en POST request kan ha samme request target?
* [ ] I forelesningen har vi sett på å innføre begrepet Controllers for å organisere logikken i serveren. Unntaket fra det som håndteres med controllers er håndtering av filer på disk. Kan dere skrive om HttpServer til å bruke en FileController for å lese filer fra disk?
* [ ] FARLIG: I løpet av kurset har HttpServer og tester fått funksjonalitet som ikke lenger er nødvendig. Klarer dere å fjerne alt som er overflødig nå uten å også fjerne kode som fortsatt har verdi? (Advarsel: Denne kan trekke ned dersom dere gjør det feil!)

### Fix:
* [ ] Databasene skal ikke kunne være null
* [x] Fikse jallaknappen AKA refresh
* [x] Test coverage
* [x] Bedre navngivning
* [ ] Bedre feilhåndtering
* [ ] Endre måten daoene lagrer fra input slik at det ikke lagres blank / null til databasen, men at det da heller sendes en 400 request error.(Eksempel kode under)
* [x] Fikse Jar URL encode/decode       
* [ ] EditSurvey lagrer gjeldende survey ID for endringer. Uten å måtte spesifisere ID hver gang
* [x] Github actions kjører rødt 



                    eksempel: if (person.getFirstName()= null || person.getFirstName().isBlank()){
                                return new HttpMessage(
                                            "HTTP/1.1 400 request error",          
                                            "<h1>First name must be provided</h1>"
                                );
                              }
