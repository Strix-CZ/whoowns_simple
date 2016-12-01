/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package text;

public class RandomWikipediaArticles {
    // This class contains first paragraphs of 50 randomly chosen articles from Wikipedia http://en.wikipedia.org
    // It is used to test context learning for WordStatistics class.
    // http://en.wikipedia.org/wiki/Special:Random
    // The text is available under the Creative Commons Attribution-ShareAlike License.

    // http://en.wikipedia.org/wiki/Iara_%28mythology%29
    // "The Iaras", bronze sculpture by Cheschiatti, at the Alvorada Palace Iara, also spelled Uiara or Yara (Portuguese pronunciation: [iˈjaɾɐ], [iˈaɾɐ], [ˈjaɾɐ], [wiˈjaɾɐ], [ujˈjaɾɐ]) or Mãe das Águas ([ˈmɐ̃j dɐˈz aɣwɐs], "mother of the water bodies"), is a figure from Brazilian mythology based on ancient Tupi and Guaraní mythology. The word derives from Old Tupi yîara = y + îara (water + lord/lady) = lady of the lake (water queen). She is seen as either a water nymph, siren, or mermaid depending upon the context of the story told about her. The Brazilian town of Nova Olinda claims the Cama da Mãe D’água as the home of Iara.

    // http://en.wikipedia.org/wiki/Almada_River
    // The Almada River is a river of Bahia state in eastern Brazil.

    // http://en.wikipedia.org/wiki/Grand_Comoro_day_gecko
    // Grand Comoro day gecko (Phelsuma v-nigra comoraegrandensis Meier, 1986) is a small diurnal subspecies of geckos. It lives in the Comoros and typically inhabits trees and bushes. The Grand Comoro day gecko feeds on insects and nectar.

    // http://en.wikipedia.org/wiki/Mamello_Makhabane
    // Mamello Makhabane is a South African football goalkeeper. She plays for Palace Super Falcons and the South Africa women's national football team.

    // http://en.wikipedia.org/wiki/Islamic_Republic_of_Iran_Border_Guard_Command
    // Islamic Republic of Iran Border Guard Command (Persian: فرماندهی مرزبانی جمهوری اسلامی ایران‎), commonly known as NAJA Border Guard (Persian: مرزبانی ناجا‎), is a subdivision of Law Enforcement Force of Islamic Republic of Iran (NAJA) and Iran's sole agency that performs border guard and control in land borders, and coast guard in maritime borders. The unit was founded in 2000, and from 1991 to 2000, the unit's duties was done by of Security deputy of NAJA. Before 1991, border control was Gendarmerie's duty.[1]

    // http://en.wikipedia.org/wiki/Pigeon_Lake_%28Alberta%29
    // Pigeon Lake is a lake in central Alberta, Canada that straddles the boundary between Leduc County and the County of Wetaskiwin No. 10. It is located close to Edmonton, Leduc and Wetaskiwin. Communities located along the lakeshore include Pigeon Lake Indian Reserve 138A, ten summer villages (Argentia Beach, Crystal Springs, Golden Days, Grandview, Itaska Beach, Ma-Me-O Beach, Norris Beach, Poplar Bay, Silver Beach and Sundance Beach), and four unincorporated communities (Fisher Home, Mission Beach, Mulhurst Bay and Village at Pigeon Lake).

    // http://en.wikipedia.org/wiki/Mahesh_%28actor%29
    // Mahesh is an Indian film actor in Malayalam cinema.[1] He came into movie industry as a hero later started doing negative roles and supporting roles.

    // http://en.wikipedia.org/wiki/Uncle_Bill_Reads_the_Funnies
    // Uncle Bill Reads the Funnies was a local Sunday morning children's television show on KAKE-TV in Wichita, Kansas. The show was hosted by Bill Boyle, who would read the color comics section of the Sunday Hutchinson News.

    // http://en.wikipedia.org/wiki/Pristimantis_pirrensis
    // Pristimantis pirrensis is a species of frog in the Craugastoridae family. It is endemic to Panama. Its natural habitat is tropical moist montane forests.

    // http://en.wikipedia.org/wiki/Zoe_Porphyrogenita
    // Zoe (in Greek: Ζωή, Zōē, meaning "Life") (c. 978 – June 1050) reigned as Byzantine Empress alongside her sister Theodora from April 19 to June 11, 1042. She was also enthroned as the Empress Consort to a series of co-rulers beginning with Romanos III in 1028 until her death in 1050 while married to Constantine IX.

    // http://en.wikipedia.org/wiki/Hong_In-young
    // Hong In-young (born October 27, 1985) is a South Korean actress. Hong represented her country at the 2005 edition of the Hong Kong-based Miss Asia Pageant, where she won Miss Photogenic and placed first runner-up. Among her prizes was a contract with pageant organizer ATV, then upon its expiration, she returned to South Korea to further her career.

    // http://en.wikipedia.org/wiki/HD_79940
    // HD 79940 is a star in the constellation Vela. Its apparent magnitude is 4.64.

    // http://en.wikipedia.org/wiki/Am%C3%A9d%C3%A9e_Joullin
    // Amédée Joullin (1862–1917) was a French American painter whose work centered on the landscapes of California and on Native Americans.

    // http://en.wikipedia.org/wiki/Dumitru_Negoi%C8%9B%C4%83
    // Dumitru Negoiţă (born February 9, 1960) is a retired male javelin thrower from Romania. He set his personal best (81.88 metres) on 22 July 1990 in Bucharest. Negoiţă is best known for winning the gold medal in the men's javelin throw event at the 1985 Summer Universiade in Kobe, Japan.

    // http://en.wikipedia.org/wiki/1978_France_rugby_union_tour_of_Far_East_and_Canada
    // The 1978 France rugby union tour of Far East and Canada was a series of matches played in September 1978 in Far East and Canada by France national rugby union team.

    // http://en.wikipedia.org/wiki/Johan_Badendyck
    // Johan Badendyck (28 March 1902 – 19 April 1973) was a Norwegian long-distance runner. He was born in Kristiania, and represented the club IL i BUL. He competed in the 3000 metres team race at the 1924 Summer Olympics, along with teammates Haakon Jansen, Nils Andersen and Hans Gundhus.

    // http://en.wikipedia.org/wiki/Children_of_Jazz
    // Children of Jazz is a 1923 American comedy silent film directed by Jerome Storm and written by Harold Brighouse and Beulah Marie Dix. The film stars Theodore Kosloff, Ricardo Cortez, Robert Cain, Eileen Percy, Irene Dalton and Alec B. Francis. The film was released on July 8, 1923, by Paramount Pictures.

    // http://en.wikipedia.org/wiki/Kopawa
    // Kopawa is a village development committee in Kapilvastu District in the Lumbini Zone of southern Nepal. At the time of the 1991 Nepal census it had a population of 7269 people living in 1142 individual households.

    // http://en.wikipedia.org/wiki/Political_funding_in_the_United_Kingdom
    // Political funding in the United Kingdom has been a source of controversy for many years.[1] There are three main ways a political party is funded. The first is through membership fees; the second is through donations; and the third is through state funding (though only for administrative costs).[2] The general restrictions in the UK were held in Bowman v United Kingdom[3] to be fully compatible with the European Convention on Human Rights, article 10.

    // http://en.wikipedia.org/wiki/2013_Dublin_Senior_Hurling_Championship
    // The 2013 Dublin Senior Hurling Championship is the 124th staging of the Dublin Senior Hurling Championship since its establishment in 1887. The championship is scheduled to end on 10 November 2013.

    // http://en.wikipedia.org/wiki/Roaring_Spring,_Kentucky
    // Roaring Spring is an unincorporated community in Trigg County, Kentucky, United States.

    // http://en.wikipedia.org/wiki/Remington_Glacier
    // Remington Glacier is a steep glacier about 7 nautical miles (13 km) long in Doyran Heights in the Sentinel Range of Ellsworth Mountains, Antarctica. It rises just north of McPherson Peak and flows east-southeast to debouch between the terminus of Hough Glacier and Johnson Spur.

    // http://en.wikipedia.org/wiki/Concord,_Kentucky
    // Concord is a class-6 city in Lewis County, Kentucky, in the United States. The population was 28 at the 2000 census. It is part of the Maysville Micropolitan Statistical Area. It is the smallest incorporated city in Kentucky by size and population.

    // http://en.wikipedia.org/wiki/John_W._Heselton
    // John Walter Heselton (March 17, 1900 – August 19, 1962) was a Republican member of the United States House of Representatives from January 3, 1945 until January 3, 1959. Heselton represented Massachusetts' first congressional district for seven consecutive terms.

    // http://en.wikipedia.org/wiki/Jamal_Alioui
    // Jamal Alioui (Arabic: جمال عليوي‎, born 2 June 1982) is a French-born Moroccan footballer. He currently plays for Wydad Casablanca. He also played for Perugia Calcio, Calcio Catania, F.C. Crotone, FC Metz, FC Sion, FC Nantes, Wydad Casablanca in the Moroccan league and Al-Kharitiyath in the Qatar Stars League.

    // http://en.wikipedia.org/wiki/Groote_Beek
    // Groote Beek is a river of Mecklenburg-Vorpommern, Germany.

    // http://en.wikipedia.org/wiki/122nd_Fighter_Aviation_Squadron
    // The 122nd Fighter Aviation Squadron (Serbo-Croatian: 122. lovačka avijacijska eskadrila / 122. ловачка авијацијска ескадрила) was an aviation squadron of Yugoslav Air Force established in April 1961 as part of 94th Fighter Aviation Regiment at Skopski Petrovac military airport. It was equipped with US-made North American F-86E Sabre jet fighter aircraft.

    // http://en.wikipedia.org/wiki/Shoot_High_Aim_Low
    // "Shoot High Aim Low" is a song by Yes. It appears on the band's 1987 Big Generator album. The song appears to have never released as a single,[1] but reached position #11 on the Mainstream Rock Tracks chart in the '80s. It appeared on every show on the Big Generator tour, but nowhere else to date.

    // http://en.wikipedia.org/wiki/Luk%C3%A1%C5%A1_Matejka
    // Lukáš Matejka (born February 21, 1990) is a Slovakian professional ice hockey defenceman who is currently playing with MHC Martin of the Slovak Extraliga.

    // http://en.wikipedia.org/wiki/Eric_Rideal
    // Sir Eric Keightley Rideal, FRS, MBE (11 April 1890 – 25 September 1974)[1] was an English physical chemist. He worked on a wide range of subjects, including electrochemistry, chemical kinetics, catalysis, electrophoresis, colloids and surface chemistry.[2] He is best known for the Eley–Rideal mechanism, which he proposed in 1938 with Daniel D. Eley.[3] He is also known for the textbook that he authored, An Introduction to Surface Chemistry (1926),[3] and was awarded honours for the research he carried out during both World Wars and for his services to chemistry.

    // http://en.wikipedia.org/wiki/Tachyon_%28software%29
    // Tachyon is a parallel/multiprocessor ray tracing software. It is a parallel ray tracing library for use on distributed memory parallel computers, shared memory computers, and clusters of workstations. It was originally developed at the University of Illinois at Urbana–Champaign. It is released under a permissive license (included in the tarball).

    // http://en.wikipedia.org/wiki/Niagara_Falls_Transit
    // Niagara Falls Transit operates the public transport bus services in Niagara Falls, Ontario, Canada. Established in 1960, Niagara Transit originally operated ten routes. The current service now provides 14 Monday to Saturday daytime routes, Eight Evening & Sunday & Holiday routes and three shuttle services for Brock University and Niagara College (Welland and Glendale). In 2007 the operation of Niagara Transit was taken over by the transportation department of the City of Niagara Falls, the following year it changed its name to Niagara Falls Transit. The motto for Niagara Falls Transit is "The way to go". On October 8, 2013 Niagara Falls City Council approved the re-routing of Niagara Falls Transit effective January 5, 2014.

    // http://en.wikipedia.org/wiki/James_A._Garfield_School
    // The James A. Garfield School is a school building located at 840 Waterman Street in Detroit, Michigan. It is also known as the Frank H. Beard School.[3] The school was listed on the National Register of Historic Places and designated a Michigan State Historic Site in 1984.

    // http://en.wikipedia.org/wiki/Hell_on_Earth_%281931_film%29
    // Hell on Earth (German: Niemandsland) is a 1931 German film directed by Victor Trivas. The film is also known as No Man's Land in France.

    // http://en.wikipedia.org/wiki/Neothais_harpa
    // Neothais harpa is a species of sea snail, a marine gastropod mollusk in the family Muricidae, the murex snails or rock snails.

    // http://en.wikipedia.org/wiki/Culture_of_Papua_New_Guinea
    // The culture of Papua New Guinea is many-sided and complex. It is estimated that more than 7000 different cultural groups exist in Papua New Guinea, and most groups have their own language. Because of this diversity, in which they take pride, many different styles of cultural expression have emerged; each group has created its own expressive forms in art, dance, weaponry, costumes, singing, music, architecture and much more. To unify the nation, the language Tok Pisin, once called Neo-Melanesian (or Pidgin English) has evolved as the lingua franca — the medium through which diverse language groups are able to communicate with one another in Parliament, in the news media, and elsewhere. People typically live in villages or dispersed hamlets which rely on the subsistence farming of sweet potatoes and taro. The principal livestock in traditional Papua New Guinea is the oceanic pig (Sus papuensis). To balance the diet, people of PNG hunt, collect wild plants, or fish — depending on the local environment and mode of subsistence. Those who become skilled at farming, hunting, or fishing — and are generous — earn a great deal of respect in Papua New Guinea.

    // http://en.wikipedia.org/wiki/Athletics_at_the_1984_Summer_Olympics_%E2%80%93_Men%27s_marathon
    // These are the official results of the Men's Marathon at the 1984 Summer Olympics in Los Angeles, California, held on Sunday August 12, 1984. The race started at 5:00 pm local time. There were 107 competitors from 59 countries. A total number of 78 athletes completed the race, with Dieudonné LaMothe from Haiti finishing in last position in 2:52:18. Twenty-nine of them did not finish. Carlos Lopes of Portugal won in 2:09:21 which set the Olympic record for 24 years.

    // http://en.wikipedia.org/wiki/Hermes_cover
    // Hermes cover is a common way of referring to an export credit guarantee (ECG) by the German Federal Government. It is also referred to as a Hermesdeckung in German.

    // http://en.wikipedia.org/wiki/Roy_F._Brissenden
    // Roy Frampton Brissenden (19 April 1919 - 13 March 1999) was a NASA physicist, engineer, teacher and inventor whose pioneering and imaginative work made possible the advancement and accomplishments of the Mercury, Gemini, Apollo, and Space Shuttle programs.

    // http://en.wikipedia.org/wiki/2007_Asian_Athletics_Championships_%E2%80%93_Women%27s_heptathlon
    // The women's heptathlon event at the 2007 Asian Athletics Championships was held in Amman, Jordan on July 25–26.

    // http://en.wikipedia.org/wiki/Renuka_Lake
    // Renuka lake is in the Sirmaur district of Himachal Pradesh in India and it is 672 m above the sea level. It is the largest lake in Himachal Pradesh, with a circumference of about 3214 m. This lake was named after the goddess Renuka. It is well connected by the road. Boating is available on the lake. A lion safari and a zoo are there at Renuka. It is the site of an annual fair held in November.

    // http://en.wikipedia.org/wiki/Subordination_%28finance%29
    // Subordination in banking and finance refers to the order of priorities in claims for ownership or interest in various assets.

    // http://en.wikipedia.org/wiki/Agri_%28Maeotae%29
    // The Agri were an ancient people dwelling along the Palus Maeotis in antiquity. Strabo describes them as living among the Maeotae, Sindi, Dandarii, Toreatae, Agri, Arrechi, Tarpetes, Sittaceni, Dosci, and Aspurgiani, among others.

    // http://en.wikipedia.org/wiki/All_Around_the_Circle
    // All Around the Circle was a Canadian variety television series which featured the music of Newfoundland and Labrador, performed in St. John's.

    // http://en.wikipedia.org/wiki/Electoral_results_for_the_district_of_Thomastown
    // This is a list of electoral results for the Electoral district of Thomastown in Victorian state elections.

    // http://en.wikipedia.org/wiki/Julio_Urquijo_Ibarra
    // Julio Urquijo Ibarra, in Basque self styled as Julio Urkixokoa,[1] was a Basque linguist, cultural activist, and a Spanish Carlist politician

    // http://en.wikipedia.org/wiki/Mondo_Rock
    // Mondo Rock was an Australian rock band formed in November 1976 by founding mainstay singer-songwriter, Ross Wilson (ex-Daddy Cool). Their second album, Chemistry was issued in July 1981, which peaked at No. 2 on the Australian Kent Music Report Albums Chart. It was followed by Nuovo Mondo in July 1982 which reached No. 7, The Modern Bop in April 1984 which appeared at No. 2 and a compilation album, Up to the Moment in June 1985, which peaked at No. 5. Mondo Rock reached the top 10 on the related Kent Music Report Singles Chart with "State of the Heart" (October 1980), "Cool World" (April 1981) and "Come Said the Boy" (December 1983). The group disbanded in 1991, although they have periodically undertaken reunion concerts. According to Australian musicologist, Ian McFarlane, "[b]y way of ceaseless touring and the release of a series of sophisticated pop rock albums, [the band was] one of the most popular acts in Australia during the early 1980s".

    // http://en.wikipedia.org/wiki/Leung_Ka-lau
    // Leung Ka-lau (born 1962 in Hong Kong with family roots in Zhaoqing, Guangdong) is the member of the Legislative Council of Hong Kong (Functional constituency, medical). He is the first public hospital doctor to be elected as a legislator. He beat Kwok Ka-ki for the seat in the Hong Kong legislative election, 2008. Dr. Leung is a surgeon specialising in General Surgery in the Prince of Wales Hospital in Shatin.

    // http://en.wikipedia.org/wiki/Baden_Landtag_elections_in_the_Weimar_Republic
    // Results of elections for the Baden Landtag, the parliament of the Republic of Baden during the Weimar Republic.

    // http://en.wikipedia.org/wiki/Phallic_Rock
    // Phallic Rock is a precambrian granite rock formation in Carefree, Arizona, United States. The formation is caused by spheroidal weathering whereby the composition of the granite and its crystal structure facilitated the development of rounded corners and its unique tubular shape.[1] The formation is at the eastern foot of Black Mountain and can be found approximately 400 feet east of Tom Darlington Drive on Stagecoach Pass Road. The formation is best viewed from the western side looking east. There is a dirt pull-off on the side of Stagecoach Pass Road with enough room for several vehicles.
}
