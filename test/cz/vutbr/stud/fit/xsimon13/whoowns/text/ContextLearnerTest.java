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

package cz.vutbr.stud.fit.xsimon13.whoowns.text;

import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class ContextLearnerTest {

    private WordStatistics stats;

    @Before
    public void createWordStatistics() throws Exception {

        ParsedClassProvider classProvider = Factory.createParsedClassProvider(TestUtils.getTestProjectRoot());
        stats = new WordStatistics(TestUtils.getTestJedis());
        stats.clear();

        ContextLearner learner = new ContextLearner(classProvider.getFileAccessor(), classProvider, stats);
        learner.learn();
    }

    @After
    public void deleteWordStatistics() throws Exception {
        if (stats != null)
            stats.clear();
        MessageBoard.getInstance().clearLastMessages();
    }

    @Test
    public void learnContextTest() throws Exception {
        TestUtils.assertNoMessages();

        Assert.assertTrue(stats.getIdfFw("ughugh") > 0);

        Assert.assertTrue(stats.getIdfFw("unique") > stats.getIdfFw("method"));
        Assert.assertTrue(stats.getIdfFw("wikipedia") > stats.getIdfFw("the"));
        Assert.assertTrue(stats.getIdfFw("nested") > stats.getIdfFw("holborn"));
        Assert.assertTrue(stats.getIdfFw("holborn") > stats.getIdfFw("to"));
        Assert.assertTrue(stats.getIdfFw("licensor") > stats.getIdfFw("to"));
        Assert.assertTrue(stats.getIdfFw("station") > stats.getIdfFw("keyword"));
        Assert.assertTrue(stats.getIdfFw("australian") > stats.getIdfFw("thesis"));
        Assert.assertTrue(stats.getIdfFw("uerl") > stats.getIdfFw("was"));
    }

    @Test
    public void testKeywordExtraction() throws Exception {
        Date time = new Date();

        for (int i = 0; i < categoryNames.length; ++i) {
            for (String document : categories[i]) {
                for (int j=0; j<10; ++j)
                    stats.analyzeCategorizedDocument(document, categoryNames[i], time);
            }
        }

        makeAssertions(stats);

        // reload and make the assertions again
        stats.save();
        stats = new WordStatistics(TestUtils.getTestJedis());

        makeAssertions(stats);
    }

    private void makeAssertions(WordStatistics stats) throws Exception {
        TestUtils.assertNoMessages();

        Assert.assertTrue(stats.getWeight("pseudorandom", pseudorandom, 1) > stats.getWeight("pseudorandom", olympics, 1));
        Assert.assertTrue(stats.getWeight("pseudorandom", pseudorandom, 1) > stats.getWeight("material", rocks, 1));

        stats.getWeight("generators", pseudorandom, 1);

        List<String> kw = stats.extractKeywordsForCategory(pseudorandom, 5, 1);
        Assert.assertTrue(kw.contains("pseudorandom"));
        Assert.assertTrue(kw.contains("generator"));
        Assert.assertTrue(kw.contains("sequence"));

        kw = stats.extractKeywordsForCategory(rocks, 5, 1);
        Assert.assertTrue(kw.contains("rock"));
        Assert.assertTrue(kw.contains("igneous"));
        Assert.assertTrue(kw.contains("anorthosite"));

        kw = stats.extractKeywordsForCategory(olympics, 5, 1);
        Assert.assertTrue(kw.contains("olympic"));
        Assert.assertTrue(kw.contains("games"));
    }


    private static final String pseudorandom = "pseudorandom";
    private static final String rocks = "rocks";
    private static final String olympics = "olympics";

    private static final String[] categoryNames = new String[]{
            pseudorandom, rocks, olympics
    };

    // The following texts are taken from Wikipedia http://en.wikipedia.org
    // It is used to test context learning, the text is the first paragraph from a randomly chosen article within a given category.
    // http://en.wikipedia.org/wiki/Special:RandomInCategory
    // The text is available under the Creative Commons Attribution-ShareAlike License.
    private static final String[][] categories = new String[][] {
            { // Category: Pseudorandom_number_generators (pseudorandom, generators, sequence)
                    // http://en.wikipedia.org/wiki/Spectral_test
                    "Spectral Test. The spectral test is a statistical test for the quality of a class of pseudorandom number generators (PRNGs), the so-called linear congruence generators (LCGs).[1] As this test is devised to study the lattice structures of LCGs, it can not be applied to other families of PRNGs. According to Donald Knuth,[2] this is by far the most powerful test known, because it can fail LCGs which pass most statistical tests. The IBM subroutine RANDU[3] LCG fails in this test for 3 dimensions and above.",
                    // http://en.wikipedia.org/wiki/RANDU
                    "RANDU. RANDU is a linear congruential pseudorandom number generator of the Park–Miller type, which has been used since the 1960s.[1] It is defined by the recurrence: with the initial seed number, \\scriptstyle V_0 as an odd number. It generates pseudorandom integers \\scriptstyle V_j which are uniformly distributed in the interval [1, 231 − 1], but in practical applications are often mapped into pseudorandom rationals \\scriptstyle X_j in the interval (0, 1), by the formula:",
                    // http://en.wikipedia.org/wiki/Pseudorandom_number_generator
                    "pseudorandom number generator. A pseudorandom number generator (PRNG), also known as a deterministic random bit generator (DRBG),[1] is an algorithm for generating a sequence of numbers whose properties approximate the properties of sequences of random numbers. The PRNG-generated sequence is not truly random, because it is completely determined by a relatively small set of initial values, called the PRNG's seed (which may include truly random values). Although sequences that are closer to truly random can be generated using hardware random number generators, pseudorandom number generators are important in practice for their speed in number generation and their reproducibility.",
                    // http://en.wikipedia.org/wiki/Inversive_congruential_generator
                    "Inversive congruential generator. Inversive congruential generators are a type of nonlinear congruential pseudorandom number generator, which use the modular multiplicative inverse (if it exists) to generate the next number in a sequence. The standard formula for an inversive congruential generator, modulo some prime q is: Such a generator is denoted symbolically as ICG(q,a,c,seed) and is said to be an ICG with parameters and seed seed.",
                    // http://en.wikipedia.org/wiki/Pseudo-random_number_sampling
                    "Pseudo-random number sampling. Pseudo-random number sampling or non-uniform pseudo-random variate generation is the numerical practice of generating pseudo-random numbers that are distributed according to a given probability distribution. Methods of sampling a non-uniform distribution are typically based on the availability of a pseudo-random number generator producing numbers X that are uniformly distributed. Computational algorithms are then used to manipulate a single random variate, X, or often several such variates, into a new random variate Y such that these values have the required distribution.",
                    // http://en.wikipedia.org/wiki/Feedback_with_Carry_Shift_Registers
                    "Feedback with Carry Shift Register. In sequence design, a Feedback with Carry Shift Register (or FCSR) is the arithmetic or with carry analog of a Linear feedback shift register (LFSR). If N >1 is an integer, then an N-ary FCSR of length r is a finite state device with a state (a;z) = (a_0,a_1,\\dots,a_{r-1};z) consisting of a vector of elements a_i in \\{0,1,\\dots,N-1\\}=S and an integer z.[1][2][3][4] The state change operation is determined by a set of coefficients and is defined as follows: compute Express s as s = a_r + N z' with a_r in S. Then the new state is (a_1,a_2,\\dots,a_r; z'). By iterating the state change an FCSR generates an infinite, eventually period sequence of numbers in S.",
                    // http://en.wikipedia.org/wiki/Xorshift
                    "Xorshift. Xorshift random number generators are a class of pseudorandom number generators that was discovered by George Marsaglia.[1] They generate the next number in their sequence by repeatedly taking the exclusive or of a number with a bit shifted version of itself. This makes them extremely fast on modern computer architectures. They are a subclass of linear feedback shift registers, but their simple implementation typically makes them faster and use less space.[2] However, the parameters have to be chosen very carefully in order to achieve a long period."
            },
            { // Category: Igneous_rocks (igneous, anorthosite)
                    // http://en.wikipedia.org/wiki/Caldera
                    "Caldera. A caldera is a cauldron-like volcanic feature usually formed by the collapse of land following a volcanic eruption. They are sometimes confused with volcanic craters. The word comes from Spanish caldera, and this from Latin caldaria, meaning \"cooking pot\". In some texts the English term cauldron is also used. The term caldera was introduced into the geological vocabulary by the German geologist Leopold von Buch when he published his memoirs of an 1815 visit to the Canary Islands, where he saw the Las Cañadas caldera on Tenerife, with Teide dominating the scene, and the Caldera de Taburiente on La Palma.",
                    // http://en.wikipedia.org/wiki/Clinopyroxene_thermobarometry
                    "Clinopyroxene Thermobarometry. In petrology the mineral clinopyroxene is used for temperature and pressure calculations of the magma that produced igneous rock containing this mineral. Clinopyroxene thermobarometry is one of several geothermobarometers. Two things makes this method especially useful; first, clinopyroxene is a common phenocryst in igneous rocks easy to identify, second, the crystallization of the jadeite component of clinopyroxene implies a growth in molar volume being thus a good indicator of pressure.",
                    // http://en.wikipedia.org/wiki/Peperite
                    "Peperite. A Peperite is a sedimentary rock that contains fragments of igneous material and is formed when magma comes into contact with wet sediments.[1] The term was originally used to describe rocks from the Limagne region of France,[2] from the similarity in appearance of the granules of dark basalt in the light-coloured limestone to black pepper. Typically the igneous fragments are glassy and show chilled-margins to the sedimentary matrix, distinguishing them from clasts with a sedimentary origin.",
                    // http://en.wikipedia.org/wiki/Anorthosite
                    "Anorthosite. Anorthosite /ænˈɔrθəsaɪt/ is a phaneritic, intrusive igneous rock characterized by a predominance of plagioclase feldspar (90–100%), and a minimal mafic component (0–10%). Pyroxene, ilmenite, magnetite, and olivine are the mafic minerals most commonly present. Anorthosite on Earth can be divided into two types: Proterozoic anorthosite (also known as massif or massif-type anorthosite) and Archean anorthosite. These two types of anorthosite have different modes of occurrence, appear to be restricted to different periods in Earth's history, and are thought to have had different origins.",
                    // http://en.wikipedia.org/wiki/Troctolite
                    "Troctolite. Troctolite is a mafic intrusive rock type. It consists essentially of major but variable amounts of olivine and calcic plagioclase along with minor pyroxene. It is an olivine-rich anorthosite, or a pyroxene-depleted relative of gabbro. However, unlike gabbro, no troctolite corresponds in composition to a partial melt of peridotite. Thus, troctolite is necessarily a cumulate of crystals that have fractionated from melt.",
                    // http://en.wikipedia.org/wiki/Tuffite
                    "Tuffite. Tuffite is a tuff containing both pyroclastic and detrital materials, but predominantly pyroclasts (Glossary of Geology). According to IUGS definition tuffite contains 75% to 25% volcanic (epiclastic) material",
                    // http://en.wikipedia.org/wiki/Migmatite
                    "Migmatite. Migmatite is a rock that is a mixture of metamorphic rock and igneous rock. It is created when a metamorphic rock such as gneiss partially melts, and then that melt recrystallizes into an igneous rock, creating a mixture of the unmelted metamorphic part with the recrystallized igneous part.[1] They can also be known as diatexite."
            },
            { // Category: Olympic Games (international, games, olympic)
                    // http://en.wikipedia.org/wiki/Paralympic_Games
                    "Paralympic Games. The Paralympic Games is a major international multi-sport event, involving athletes with a range of physical disabilities, including impaired muscle power (e.g. paraplegia and quadriplegia, muscular dystrophy, Post-polio syndrome, spina bifida), impaired passive range of movement, limb deficiency (e.g. amputation or dysmelia), leg length difference, short stature, hypertonia, ataxia, athetosis, vision impairment and intellectual impairment. There are Winter and Summer Paralympic Games, which since the 1988 Summer Games in Seoul, South Korea, are held almost immediately following the respective Olympic Games. All Paralympic Games are governed by the International Paralympic Committee (IPC).",
                    // http://en.wikipedia.org/wiki/Olympic_Cup
                    "Olympic Cup. The Olympic Cup (French: Coupe olympique) is an award given annually by the International Olympic Committee. It was instituted by Pierre de Coubertin in 1906 and is awarded to an institution or association with a record of merit and integrity in actively developing the Olympic Movement.",
                    // http://en.wikipedia.org/wiki/International_Children%27s_Games
                    "International Children's Games. The International Children's Games (ICG) is an International Olympic Committee-sanctioned event[1] held every year where children from cities around the world and between the ages of 12 and 15 participate in a variety of sports and cultural activities.",
                    // http://en.wikipedia.org/wiki/Olympic_winners_of_the_Archaic_period
                    "Olympic winners of the Archaic period. Just how far back in history organized athletic contests were held remains a matter of debate, but it is reasonably certain that they occurred in Greece almost 3,000 years ago. However ancient in origin, by the end of the 6th century BC at least four Greek sporting festivals, sometimes called \"classical games,\" had achieved major importance: the Olympic Games, held at Olympia; the Pythian Games at Delphi; the Nemean Games at Nemea; and the Isthmian Games, held near Corinth.[1] The Olympic Games was perhaps the greatest of all sporting event held every four years and all Olympian winners, were highly appreciated among the Greeks.",
                    // http://en.wikipedia.org/wiki/Olympiad
                    "Olympiad. An Olympiad (Greek: Ὀλυμπιάς, olympiás) is a period of four years associated with the Olympic Games of the Ancient Greeks. During the Hellenistic period, beginning with Ephorus, it was used as a calendar epoch. By this reckoning, the first Olympiad lasted from the summer of 776 BC to that of 772 BC. By extrapolation to the Gregorian calendar, the 3rd year of the 698th Olympiad begins in (Northern-Hemisphere) mid-summer 2015.",
                    // http://en.wikipedia.org/wiki/Olympic_Park
                    "Olympic Park. An Olympic Park is a sports campus for hosting the Olympic Games. Typically it contains the Olympic Stadium and the International Broadcast Centre. It may also contain the Olympic Village or some of the other sports venues, such as the aquatics complex in the case of the summer games, or the main ice hockey rink for the winter games. The Olympic Park is part of the \"legacy\" which provides benefit to the host city after the games have ended. As such it may subsequently include an urban park and a museum or similar commemoration of the games that were hosted there.",
                    // http://en.wikipedia.org/wiki/Olympic_Green
                    "Olympic Green. The Olympic Green (Simplified: 北京奥林匹克公园, Traditional: 北京奧林匹克公園, Pinyin: Běijīng Àolínpǐkè Gōngyuán) is an Olympic Park in Chaoyang District, Beijing, China constructed for the 2008 Summer Olympics. Since then, the streets around the park have been used for an exhibition street race of the FIA GT1 World Championship in 2011, after a race at Goldenport Park Circuit in the vicinity."
            }
    };
}
