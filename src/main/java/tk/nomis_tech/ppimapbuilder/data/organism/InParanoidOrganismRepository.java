package tk.nomis_tech.ppimapbuilder.data.organism;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repository of InParanoid supported Organisms
 */
public class InParanoidOrganismRepository {

	private static InParanoidOrganismRepository _instance;
	private final List<Organism> organisms;

	private InParanoidOrganismRepository() {
		organisms = Arrays.asList(
				new Organism("Rhodotorula glutinis (strain ATCC 204091 / IIP 30 / MTCC 1151)", "RHOG2", "Yeast", 1001064),
				new Organism("Cricetulus griseus", "CRIGR", "Chinese hamster", 10029),
				new Organism("Edhazardia aedis (strain USNM 41457)", "EDHAE", "Microsporidian parasite", 1003232),
				new Organism("Heterocephalus glaber", "HETGA", "Naked mole rat", 10181),
				new Organism("Fusarium pseudograminearum (strain CS3096)", "FUSPC", "Wheat and barley crown-rot fungus", 1028729),
				new Organism("Aspergillus kawachii (strain NBRC 4308)", "ASPKW", "White koji mold", 1033177),
				new Organism("Acromyrmex echinatior", "ACREC", "Panamanian leafcutter ant", 103372),
				new Organism("Camponotus floridanus", "CAMFO", "Florida carpenter ant", 104421),
				new Organism("Naumovozyma castellii (strain ATCC 76901 / CBS 4309 / NBRC 1992 / NRRL Y-12630)", "NAUCC", "Yeast", 1064592),
				new Organism("Tetrapisispora blattae (strain ATCC 34711 / CBS 6284 / DSM 70876 / NBRC 10599 / NRRL Y-10934 / UCD 77-7)", "TETBL", "Yeast", 1071380),
				new Organism("Kazachstania africana (strain ATCC 22294 / BCRC 22015 / CBS 2517 / CECT 1963 / NBRC 1671 / NRRL Y-8276)", "KAZAF", "Yeast", 1071382),
				new Organism("Marssonina brunnea f. sp. multigermtubi (strain MB_m1)", "MARBU", "Marssonina leaf spot fungus", 1072389),
				new Organism("Torulaspora delbrueckii (strain ATCC 10662 / CBS 1146 / NBRC 0425 / NCYC 2629 / NRRL Y-866)", "TORDC", "Yeast", 1076872),
				new Organism("Fibroporia radiculosa (strain TFFH 294)", "FIBRA", "Brown rot fungus", 1078123),
				new Organism("Phytophthora sojae (strain P6497)", "PHYSP", "Soybean stem and root rot agent", 1094619),
				new Organism("Glarea lozoyensis (strain ATCC 74030 / MF5533)", "GLAL7", "", 1104152),
				new Organism("Piriformospora indica (strain DSM 11827)", "PIRID", "", 1109443),
				new Organism("Hordeum vulgare var. distichum", "HORVD", "Two-rowed barley", 112509),
				new Organism("Macrophomina phaseolina (strain MS6)", "MACPH", "Charcoal rot fungus", 1126212),
				new Organism("Penicillium digitatum (strain PHI26 / CECT 20796)", "PEND2", "Green mold", 1170229),
				new Organism("Trichosporon asahii var. asahii (strain ATCC 90039 / CBS 2479 / JCM 2466 / KCTC 7840 / NCYC 2677 / UAMH 7654)", "TRIAS", "Yeast", 1186058),
				new Organism("Wickerhamomyces ciferrii (strain F-60-10 / ATCC 14091 / CBS 111 / JCM 3599 / NBRC 0793 / NRRL Y-1031)", "WICCF", "Yeast", 1206466),
				new Organism("Pneumocystis jiroveci (strain SE8)", "PNEJ8", "Pneumocystis pneumonia agent", 1209962),
				new Organism("Colletotrichum gloeosporioides (strain Nara gc5)", "COLGN", "Anthracnose fungus", 1213859),
				new Organism("Blastocystis hominis", "BLAHO", "", 12968),
				new Organism("Danaus plexippus", "DANPL", "Monarch butterfly", 13037),
				new Organism("Caenorhabditis brenneri", "CAEBE", "Nematode worm", 135651),
				new Organism("Solenopsis invicta", "SOLIN", "Red imported fire ant", 13686),
				new Organism("Magnaporthe oryzae (strain 70-15 / ATCC MYA-4617 / FGSC 8958)", "MAGO7", "Rice blast fungus", 242507),
				new Organism("Tupaia chinensis", "TUPCH", "Chinese tree shrew", 246437),
				new Organism("Kluyveromyces lactis (strain ATCC 8585 / CBS 2359 / DSM 70799 / NBRC 1267 / NRRL Y-1140 / WM37)", "KLULA", "Yeast", 284590),
				new Organism("Debaryomyces hansenii (strain ATCC 36239 / CBS 767 / JCM 1990 / NBRC 0083 / IGC 2968)", "DEBHA", "Yeast", 284592),
				new Organism("Candida glabrata (strain ATCC 2001 / CBS 138 / JCM 3761 / NBRC 0622 / NRRL Y-65)", "CANGA", "Yeast", 284593),
				new Organism("Ectocarpus siliculosus", "ECTSI", "Brown alga", 2880),
				new Organism("Crassostrea gigas", "CRAGI", "Pacific oyster", 29159),
				new Organism("Meyerozyma guilliermondii (strain ATCC 6260 / CBS 566 / DSM 6381 / JCM 1539 / NBRC 10279 / NRRL Y-324)", "PICGU", "Yeast", 294746),
				new Organism("Micromonas sp. (strain RCC299 / NOUM17)", "MICSR", "Picoplanktonic green alga", 296587),
				new Organism("Volvox carteri", "VOLCA", "Green alga", 3067),
				new Organism("Chaetomium globosum (strain ATCC 6205 / CBS 148.51 / DSM 1962 / NBRC 6347 / NRRL 1970)", "CHAGB", "Soil fungus", 306901),
				new Organism("Clavispora lusitaniae (strain ATCC 42720)", "CLAL4", "Yeast", 306902),
				new Organism("Caenorhabditis remanei", "CAERE", "", 31234),
				new Organism("Scheffersomyces stipitis (strain ATCC 58785 / CBS 6054 / NBRC 10063 / NRRL Y-11545)", "PICST", "Yeast", 322104),
				new Organism("Mycosphaerella graminicola (strain CBS 115943 / IPO323)", "MYCGM", "Speckled leaf blotch fungus", 336722),
				new Organism("Uncinocarpus reesii (strain UAMH 1704)", "UNCRE", "", 336963),
				new Organism("Oikopleura dioica", "OIKDI", "Tunicate", 34765),
				new Organism("Cryptosporidium parvum (strain Iowa II)", "CRYPI", "", 353152),
				new Organism("Lodderomyces elongisporus (strain ATCC 11503 / CBS 2605 / JCM 1781 / NBRC 1676 / NRRL YB-4239)", "LODEL", "Yeast", 379508),
				new Organism("Ricinus communis", "RICCO", "Castor bean", 3988),
				new Organism("Hypocrea virens (strain Gv29-8 / FGSC 10586)", "HYPVG", "Gliocladium virens", 413071),
				new Organism("Perkinsus marinus (strain ATCC 50983 / TXsc)", "PERM5", "", 423536),
				new Organism("Malassezia globosa (strain ATCC MYA-4612 / CBS 7966)", "MALGO", "Dandruff-associated fungus", 425265),
				new Organism("Pyrenophora tritici-repentis (strain Pt-1C-BFP)", "PYRTR", "Wheat tan spot fungus", 426418),
				new Organism("Vanderwaltozyma polyspora (strain ATCC 22028 / DSM 70294)", "VANPO", "", 436907),
				new Organism("Aureococcus anophagefferens", "AURAN", "Harmful bloom alga", 44056),
				new Organism("Talaromyces stipitatus (strain ATCC 10500 / CBS 375.48 / QM 6759 / NRRL 1006)", "TALSN", "", 441959),
				new Organism("Setaria italica", "SETIT", "Foxtail millet", 4555),
				new Organism("Enterocytozoon bieneusi (strain H348)", "ENTBH", "Microsporidian parasite", 481877),
				new Organism("Paracoccidioides brasiliensis (strain Pb03)", "PARBP", "", 482561),
				new Organism("Laccaria bicolor (strain S238N-H82 / ATCC MYA-4686)", "LACBS", "Bicoloured deceiver", 486041),
				new Organism("Verticillium dahliae (strain VdLs.17 / ATCC MYA-4575 / FGSC 10137)", "VERDV", "Verticillium wilt", 498257),
				new Organism("Brassica rapa subsp. pekinensis", "BRARP", "Chinese cabbage", 51351),
				new Organism("Arthroderma gypseum (strain ATCC MYA-4604 / CBS 118893)", "ARTGP", "", 535722),
				new Organism("Chlorella variabilis", "CHLVA", "Green alga", 554065),
				new Organism("Moniliophthora perniciosa (strain FA553 / isolate CP02)", "MONPE", "Witches'-broom disease fungus", 554373),
				new Organism("Lachancea thermotolerans (strain ATCC 56472 / CBS 6340 / NRRL Y-8284)", "LACTC", "Yeast", 559295),
				new Organism("Pichia sorbitophila (strain ATCC MYA-4447 / BCRC 22081 / CBS 7064 / NBRC 10061 / NRRL Y-12695)", "PICSO", "Hybrid yeast", 559304),
				new Organism("Trichophyton rubrum (strain ATCC MYA-4607 / CBS 118892)", "TRIRC", "Athlete's foot fungus", 559305),
				new Organism("Zygosaccharomyces rouxii (strain ATCC 2623 / CBS 732 / NBRC 1130 / NCYC 568 / NRRL Y-229)", "ZYGRC", "", 559307),
				new Organism("Hyaloperonospora arabidopsidis (strain Emoy2)", "HYAAE", "Downy mildew agent", 559515),
				new Organism("Postia placenta (strain ATCC 44394 / Madison 698-R)", "POSPM", "Brown rot fungus", 561896),
				new Organism("Neospora caninum (strain Liverpool)", "NEOCL", "", 572307),
				new Organism("Thielavia heterothallica (strain ATCC 42464 / BCRC 31852 / DSM 1799)", "THIHA", "", 573729),
				new Organism("Naegleria gruberi", "NAEGR", "Amoeba", 5762),
				new Organism("Serpula lacrymans var. lacrymans (strain S7.9)", "SERL9", "Dry rot fungus", 578457),
				new Organism("Schizophyllum commune (strain H4-8 / FGSC 9210)", "SCHCM", "Split gill fungus", 578458),
				new Organism("Nosema ceranae (strain BRL01)", "NOSCE", "Microsporidian parasite", 578460),
				new Organism("Capsaspora owczarzaki (strain ATCC 30864)", "CAPO3", "", 595528),
				new Organism("Agaricus bisporus var. burnettii (strain JB137-S8 / ATCC MYA-4627 / FGSC 10392)", "AGABU", "White button mushroom", 597362),
				new Organism("Harpegnathos saltator", "HARSA", "Jerdon's jumping ant", 610380),
				new Organism("Spathaspora passalidarum (strain NRRL Y-27907 / 11-Y1)", "SPAPN", "", 619300),
				new Organism("Brugia malayi", "BRUMA", "Filarial nematode worm", 6279),
				new Organism("Wuchereria bancrofti", "WUCBA", "", 6293),
				new Organism("Trichinella spiralis", "TRISP", "Trichina worm", 6334),
				new Organism("Komagataella pastoris (strain GS115 / ATCC 20864)", "PICPG", "Yeast", 644223),
				new Organism("Gaeumannomyces graminis var. tritici (strain R3-111a-1)", "GAGT3", "Wheat and barley take-all root rot fungus", 644352),
				new Organism("Phanerochaete carnosa (strain HHB-10118-sp)", "PHACS", "White-rot fungus", 650164),
				new Organism("Pythium ultimum", "PYTUL", "", 65071),
				new Organism("Beauveria bassiana (strain ARSEF 2860)", "BEAB2", "White muscardine disease fungus", 655819),
				new Organism("Metarhizium acridum (strain CQMa 102)", "METAQ", "", 655827),
				new Organism("Grosmannia clavigera (strain kw1407 / UAMH 11150)", "GROCL", "Blue stain fungus", 655863),
				new Organism("Pseudogymnoascus destructans (strain ATCC MYA-4855 / 20631-21)", "PSED2", "Bat white-nose syndrome fungus", 658429),
				new Organism("Wallemia sebi (strain ATCC MYA-4683 / CBS 633.66)", "WALSC", "", 671144),
				new Organism("Acyrthosiphon pisum", "ACYPI", "Pea aphid", 7029),
				new Organism("Ostreococcus tauri", "OSTTA", "", 70448),
				new Organism("Tribolium castaneum", "TRICA", "Red flour beetle", 7070),
				new Organism("Culex quinquefasciatus", "CULQU", "Southern house mosquito", 7176),
				new Organism("Auricularia delicata (strain TFB10046)", "AURDE", "White-rot fungus", 717982),
				new Organism("Loa loa", "LOALO", "Eye worm", 7209),
				new Organism("Drosophila ananassae", "DROAN", "Fruit fly", 7217),
				new Organism("Drosophila grimshawi", "DROGR", "Fruit fly", 7222),
				new Organism("Drosophila mojavensis", "DROMO", "Fruit fly", 7230),
				new Organism("Trachipleistophora hominis", "TRAHO", "Microsporidian parasite", 72359),
				new Organism("Drosophila virilis", "DROVI", "Fruit fly", 7244),
				new Organism("Drosophila willistoni", "DROWI", "Fruit fly", 7260),
				new Organism("Nasonia vitripennis", "NASVI", "Parasitic wasp", 7425),
				new Organism("Melampsora larici-populina (strain 98AG31 / pathotype 3-4-7)", "MELLP", "Poplar leaf rust fungus", 747676),
				new Organism("Arthrobotrys oligospora (strain ATCC 24927 / CBS 115.81 / DSM 1491)", "ARTOA", "Nematode-trapping fungus", 756982),
				new Organism("Mixia osmundae (strain CBS 9802 / IAM 14324 / JCM 22182 / KY 12970)", "MIXOS", "", 764103),
				new Organism("Sordaria macrospora (strain ATCC MYA-333 / DSM 997 / K(L3346) / K-hell)", "SORMK", "", 771870),
				new Organism("Clonorchis sinensis", "CLOSI", "Chinese liver fluke", 79923),
				new Organism("Xiphophorus maculatus", "XIPMA", "Southern platyfish", 8083),
				new Organism("Ichthyophthirius multifiliis (strain G5)", "ICHMG", "White spot disease agent", 857967),
				new Organism("Exophiala dermatitidis (strain ATCC 34100 / CBS 525.76 / NIH/UT8656)", "EXODN", "Black yeast", 858893),
				new Organism("Selaginella moellendorffii", "SELML", "Spikemoss", 88036),
				new Organism("Nematocida parisii (strain ERTm1 / ATCC PRA-289)", "NEMP1", "Nematode killer fungus", 881290),
				new Organism("Eremothecium cymbalariae (strain CBS 270.75 / DBVPG 7215 / KCTC 17166 / NRRL Y-17582)", "ERECY", "Yeast", 931890),
				new Organism("Pteropus alecto", "PTEAL", "Black flying fox", 9402),
				new Organism("Salpingoeca rosetta (strain ATCC 50818 / BSB-021)", "SALR5", "", 946362),
				new Organism("Vavraia culicis (isolate floridensis)", "VAVCU", "Microsporidian parasite", 948595),
				new Organism("Mustela putorius furo", "MUSPF", "European domestic ferret", 9669),
				new Organism("Felis catus", "FELCA", "Cat", 9685),
				new Organism("Cordyceps militaris (strain CM01)", "CORMM", "Caterpillar fungus", 983644),
				new Organism("Leptosphaeria maculans (strain JN3 / isolate v23.1.3 / race Av1-4-5-6-7-8)", "LEPMJ", "Blackleg fungus", 985895),
				new Organism("Vittaforma corneae (strain ATCC 50505)", "VITCO", "Microsporidian parasite", 993615),
				new Organism("Sporisorium reilianum (strain SRZ2)", "SPORE", "Maize head smut fungus", 999809),
				new Organism("Botryotinia fuckeliana (strain T4)", "BOTF4", "Noble rot fungus", 999810),
				new Organism("Streptomyces coelicolor (strain ATCC BAA-471 / A3(2) / M145)", "STRCO", "", 100226),
				new Organism("Mus musculus", "MOUSE", "Mouse", 10090),
				new Organism("Rattus norvegicus", "RAT", "Rat", 10116),
				new Organism("Cavia porcellus", "CAVPO", "Guinea pig", 10141),
				new Organism("Trichoplax adhaerens", "TRIAD", "", 10228),
				new Organism("Synechocystis sp. (strain PCC 6803 / Kazusa)", "SYNY3", "", 1111708),
				new Organism("Pediculus humanus subsp. corporis", "PEDHC", "Body louse", 121224),
				new Organism("Plasmodium vivax (strain Salvador I)", "PLAVS", "", 126793),
				new Organism("Atta cephalotes", "ATTCE", "Leafcutter ant", 12957),
				new Organism("Monodelphis domestica", "MONDO", "Gray short-tailed opossum", 13616),
				new Organism("Polysphondylium pallidum", "POLPA", "Cellular slime mold", 13642),
				new Organism("Brachypodium distachyon", "BRADI", "Purple false brome", 15368),
				new Organism("Phytophthora ramorum", "PHYRM", "Sudden oak death agent", 164328),
				new Organism("Mycobacterium tuberculosis", "MYCTX", "", 1773),
				new Organism("Pyrobaculum aerophilum (strain ATCC 51768 / IM2 / DSM 7523 / JCM 9630 / NBRC 100827)", "PYRAE", "", 178306),
				new Organism("Giardia intestinalis (strain ATCC 50803 / WB clone C6)", "GIAIC", "", 184922),
				new Organism("Methanosarcina acetivorans (strain ATCC 35395 / DSM 2834 / JCM 12185 / C2A)", "METAC", "", 188937),
				new Organism("Leptospira interrogans serogroup Icterohaemorrhagiae serovar Lai (strain 56601)", "LEPIN", "", 189518),
				new Organism("Fusobacterium nucleatum subsp. nucleatum (strain ATCC 25586 / CIP 101130 / JCM 8532 / LMG 13131)", "FUSNN", "", 190304),
				new Organism("Pseudomonas aeruginosa (strain ATCC 15692 / PAO1 / 1C / PRS 101 / LMG 12228)", "PSEAE", "", 208964),
				new Organism("Cryptococcus neoformans var. neoformans serotype D (strain JEC21 / ATCC MYA-565)", "CRYNJ", "", 214684),
				new Organism("Bacillus subtilis (strain 168)", "BACSU", "", 224308),
				new Organism("Aquifex aeolicus (strain VF5)", "AQUAE", "", 224324),
				new Organism("Bradyrhizobium diazoefficiens (strain JCM 10833 / IAM 13628 / NBRC 14792 / USDA 110)", "BRADU", "", 224911),
				new Organism("Bacteroides thetaiotaomicron (strain ATCC 29148 / DSM 2079 / NCTC 10582 / E50 / VPI-5482)", "BACTN", "", 226186),
				new Organism("Emericella nidulans (strain FGSC A4 / ATCC 38163 / CBS 112.46 / NRRL 194 / M139)", "EMENI", "", 227321),
				new Organism("Gibberella zeae (strain PH-1 / ATCC MYA-4620 / FGSC 9075 / NRRL 31084)", "GIBZE", "Wheat head blight fungus", 229533),
				new Organism("Candida albicans (strain SC5314 / ATCC MYA-2876)", "CANAL", "Yeast", 237561),
				new Organism("Ustilago maydis (strain 521 / FGSC 9021)", "USTMA", "Corn smut fungus", 237631),
				new Organism("Cryptosporidium hominis", "CRYHO", "", 237895),
				new Organism("Coprinopsis cinerea (strain Okayama-7 / 130 / ATCC MYA-4618 / FGSC 9003)", "COPC7", "Inky cap fungus", 240176),
				new Organism("Rhodopirellula baltica (strain SH1)", "RHOBA", "", 243090),
				new Organism("Deinococcus radiodurans (strain ATCC 13939 / DSM 20539 / JCM 16871 / LMG 4051 / NBRC 15346 / NCIMB 9279 / R1 / VKM B-1422)", "DEIRA", "", 243230),
				new Organism("Geobacter sulfurreducens (strain ATCC 51573 / DSM 12127 / PCA)", "GEOSL", "", 243231),
				new Organism("Methanocaldococcus jannaschii (strain ATCC 43067 / DSM 2661 / JAL-1 / JCM 10045 / NBRC 100440)", "METJA", "", 243232),
				new Organism("Thermotoga maritima (strain ATCC 43589 / MSB8 / DSM 3109 / JCM 10099)", "THEMA", "", 243274),
				new Organism("Rhizopus delemar (strain RA 99-880 / ATCC MYA-4621 / FGSC 9543 / NRRL 43880)", "RHIO9", "Mucormycosis agent", 246409),
				new Organism("Coccidioides immitis (strain RS)", "COCIM", "Valley fever fungus", 246410),
				new Organism("Gloeobacter violaceus (strain PCC 7421)", "GLOVI", "", 251221),
				new Organism("Chlamydia trachomatis (strain D/UW-3/Cx)", "CHLTR", "", 272561),
				new Organism("Sulfolobus solfataricus (strain ATCC 35092 / DSM 1617 / JCM 11322 / P2)", "SULSO", "", 273057),
				new Organism("Caenorhabditis japonica", "CAEJA", "", 281687),
				new Organism("Anolis carolinensis", "ANOCA", "Green anole", 28377),
				new Organism("Yarrowia lipolytica (strain CLIB 122 / E 150)", "YARLI", "Yeast", 284591),
				new Organism("Ashbya gossypii (strain ATCC 10895 / CBS 109.51 / FGSC 9923 / NRRL Y-1056)", "ASHGO", "Yeast", 284811),
				new Organism("Schizosaccharomyces pombe (strain 972 / ATCC 24843)", "SCHPO", "Fission yeast", 284812),
				new Organism("Encephalitozoon cuniculi (strain GB-M1)", "ENCCU", "Microsporidian parasite", 284813),
				new Organism("Thermodesulfovibrio yellowstonii (strain ATCC 51303 / DSM 11347 / YP87)", "THEYD", "", 289376),
				new Organism("Vitis vinifera", "VITVI", "Grape", 29760),
				new Organism("Chlamydomonas reinhardtii", "CHLRE", "", 3055),
				new Organism("Otolemur garnettii", "OTOGA", "Small-eared galago", 30611),
				new Organism("Takifugu rubripes", "TAKRU", "Japanese pufferfish", 31033),
				new Organism("Tetrahymena thermophila (strain SB210)", "TETTS", "", 312017),
				new Organism("Phaeosphaeria nodorum (strain SN15 / ATCC MYA-4574 / FGSC 10173)", "PHANO", "Glume blotch fungus", 321614),
				new Organism("Chloroflexus aurantiacus (strain ATCC 29366 / DSM 635 / J-10-fl)", "CHLAA", "", 324602),
				new Organism("Neosartorya fumigata (strain ATCC MYA-4609 / Af293 / CBS 101355 / FGSC A1100)", "ASPFU", "", 330879),
				new Organism("Thalassiosira pseudonana", "THAPS", "Marine diatom", 35128),
				new Organism("Plasmodium falciparum (isolate 3D7)", "PLAF7", "", 36329),
				new Organism("Neurospora crassa (strain ATCC 24698 / 74-OR23-1A / CBS 708.71 / DSM 1257 / FGSC 987)", "NEUCR", "", 367110),
				new Organism("Populus trichocarpa", "POPTR", "Western balsam poplar", 3694),
				new Organism("Arabidopsis thaliana", "ARATH", "Mouse-ear cress", 3702),
				new Organism("Korarchaeum cryptofilum (strain OPF8)", "KORCO", "", 374847),
				new Organism("Glycine max", "SOYBN", "Soybean", 3847),
				new Organism("Oryza sativa subsp. japonica", "ORYSJ", "Rice", 39947),
				new Organism("Amphimedon queenslandica", "AMPQE", "Sponge", 400682),
				new Organism("Phytophthora infestans (strain T30-4)", "PHYIT", "Potato late blight fungus", 403677),
				new Organism("Solanum lycopersicum", "SOLLC", "Tomato", 4081),
				new Organism("Solanum tuberosum", "SOLTU", "Potato", 4113),
				new Organism("Puccinia graminis f. sp. tritici (strain CRL 75-36-700-3 / race SCCL)", "PUCGT", "Black stem rust fungus", 418459),
				new Organism("Anopheles darlingi", "ANODA", "Mosquito", 43151),
				new Organism("Spermophilus tridecemlineatus", "SPETR", "Thirteen-lined ground squirrel", 43179),
				new Organism("Dictyostelium discoideum", "DICDI", "Slime mold", 44689),
				new Organism("Ajellomyces capsulatus (strain G186AR / H82 / ATCC MYA-2454 / RMSCC 2432)", "AJECG", "Darling's disease fungus", 447093),
				new Organism("Nematostella vectensis", "NEMVE", "Starlet sea anemone", 45351),
				new Organism("Sorghum bicolor", "SORBI", "Sorghum", 4558),
				new Organism("Drosophila pseudoobscura pseudoobscura", "DROPS", "Fruit fly", 46245),
				new Organism("Ciona savignyi", "CIOSA", "Pacific transparent sea squirt", 51511),
				new Organism("Dictyoglomus turgidum (strain Z-1310 / DSM 6724)", "DICTD", "", 515635),
				new Organism("Pristionchus pacificus", "PRIPA", "Parasitic nematode", 54126),
				new Organism("Phaeodactylum tricornutum (strain CCAP 1055/1)", "PHATC", "", 556484),
				new Organism("Saccharomyces cerevisiae (strain ATCC 204508 / S288c)", "YEAST", "Baker's yeast", 559292),
				new Organism("Leishmania braziliensis", "LEIBR", "", 5660),
				new Organism("Leishmania major", "LEIMA", "", 5664),
				new Organism("Leishmania infantum", "LEIIN", "", 5671),
				new Organism("Trypanosoma cruzi", "TRYCR", "", 5693),
				new Organism("Trichomonas vaginalis", "TRIVA", "", 5722),
				new Organism("Entamoeba histolytica", "ENTHI", "", 5759),
				new Organism("Dictyostelium purpureum", "DICPU", "Slime mold", 5786),
				new Organism("Toxoplasma gondii", "TOXGO", "", 5811),
				new Organism("Plasmodium berghei (strain Anka)", "PLABA", "", 5823),
				new Organism("Plasmodium chabaudi", "PLACH", "", 5825),
				new Organism("Plasmodium knowlesi (strain H)", "PLAKH", "", 5851),
				new Organism("Babesia bovis", "BABBO", "", 5865),
				new Organism("Theileria annulata", "THEAN", "", 5874),
				new Organism("Theileria parva", "THEPA", "East coast fever infection agent", 5875),
				new Organism("Paramecium tetraurelia", "PARTE", "", 5888),
				new Organism("Myotis lucifugus", "MYOLU", "Little brown bat", 59463),
				new Organism("Taeniopygia guttata", "TAEGU", "Zebra finch", 59729),
				new Organism("Schistosoma mansoni", "SCHMA", "Blood fluke", 6183),
				new Organism("Nomascus leucogenys", "NOMLE", "Northern white-cheeked gibbon", 61853),
				new Organism("Caenorhabditis briggsae", "CAEBR", "", 6238),
				new Organism("Caenorhabditis elegans", "CAEEL", "", 6239),
				new Organism("Halobacterium salinarum (strain ATCC 700922 / JCM 11081 / NRC-1)", "HALSA", "", 64091),
				new Organism("Tuber melanosporum (strain Mel28)", "TUBMM", "Perigord black truffle", 656061),
				new Organism("Nectria haematococca (strain 77-13-4 / ATCC MYA-4622 / FGSC 9596 / MPVI)", "NECH7", "", 660122),
				new Organism("Sclerotinia sclerotiorum (strain ATCC 18683 / 1980 / Ss-1)", "SCLS1", "White mold", 665079),
				new Organism("Daphnia pulex", "DAPPU", "Water flea", 6669),
				new Organism("Batrachochytrium dendrobatidis (strain JAM81 / FGSC 10211)", "BATDJ", "Frog chytrid fungus", 684364),
				new Organism("Thermococcus kodakaraensis (strain ATCC BAA-918 / JCM 12380 / KOD1)", "THEKO", "", 69014),
				new Organism("Gasterosteus aculeatus", "GASAC", "Three-spined stickleback", 69293),
				new Organism("Ixodes scapularis", "IXOSC", "Black-legged tick", 6945),
				new Organism("Bombyx mori", "BOMMO", "Silk moth", 7091),
				new Organism("Aedes aegypti", "AEDAE", "Yellowfever mosquito", 7159),
				new Organism("Anopheles gambiae", "ANOGA", "African malaria mosquito", 7165),
				new Organism("Drosophila melanogaster", "DROME", "Fruit fly", 7227),
				new Organism("Plasmodium yoelii yoelii", "PLAYO", "", 73239),
				new Organism("Apis mellifera", "APIME", "Honeybee", 7460),
				new Organism("Strongylocentrotus purpuratus", "STRPU", "Purple sea urchin", 7668),
				new Organism("Ciona intestinalis", "CIOIN", "Transparent sea squirt", 7719),
				new Organism("Branchiostoma floridae", "BRAFL", "Florida lancelet", 7739),
				new Organism("Latimeria chalumnae", "LATCH", "West Indian ocean coelacanth", 7897),
				new Organism("Danio rerio", "DANRE", "Zebrafish", 7955),
				new Organism("Oryzias latipes", "ORYLA", "Medaka fish", 8090),
				new Organism("Oreochromis niloticus", "ORENI", "Nile tilapia", 8128),
				new Organism("Monosiga brevicollis", "MONBE", "Choanoflagellate", 81824),
				new Organism("Escherichia coli (strain K12)", "ECOLI", "", 83333),
				new Organism("Xenopus tropicalis", "XENTR", "Western clawed frog", 8364),
				new Organism("Gallus gallus", "CHICK", "Chicken", 9031),
				new Organism("Meleagris gallopavo", "MELGA", "Common turkey", 9103),
				new Organism("Ornithorhynchus anatinus", "ORNAN", "Duckbill platypus", 9258),
				new Organism("Sarcophilus harrisii", "SARHA", "Tasmanian devil", 9305),
				new Organism("Callithrix jacchus", "CALJA", "White-tufted-ear marmoset", 9483),
				new Organism("Macaca mulatta", "MACMU", "Rhesus macaque", 9544),
				new Organism("Gorilla gorilla gorilla", "GORGO", "Lowland gorilla", 9595),
				new Organism("Pan troglodytes", "PANTR", "Chimpanzee", 9598),
				new Organism("Pongo abelii", "PONAB", "Sumatran orangutan", 9601),
				new Organism("Homo sapiens", "HUMAN", "Human", 9606),
				new Organism("Canis familiaris", "CANFA", "Dog", 9615),
				new Organism("Ailuropoda melanoleuca", "AILME", "Giant panda", 9646),
				new Organism("Loxodonta africana", "LOXAF", "African elephant", 9785),
				new Organism("Equus caballus", "HORSE", "Horse", 9796),
				new Organism("Sus scrofa", "PIG", "Pig", 9823),
				new Organism("Bos taurus", "BOVIN", "Bovine", 9913),
				new Organism("Oryctolagus cuniculus", "RABIT", "Rabbit", 9986),
				new Organism("Tetraodon nigroviridis", "TETNG", "Spotted green pufferfish", 99883),
				new Organism("Trypanosoma brucei brucei (strain 927/4 GUTat10.1)", "TRYB2", "", 999953)
		);
	}

	public static InParanoidOrganismRepository getInstance() {
		if (_instance == null)
			_instance = new InParanoidOrganismRepository();
		return _instance;
	}

	public List<Organism> getOrganisms() throws IOException {
		return organisms;
	}
	
	public List<String> getOrganismNames() {
		ArrayList<String> names = new ArrayList<String>();
		
		try {
			for (Organism o : getOrganisms()) {
				names.add(o.getScientificName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return names;
		
	}
	
	public Organism getOrganismByScientificName(String scientificName) {
		for (Organism o : organisms) {
			if (o.getScientificName().equals(scientificName)) {
				return o;
			}
		}
		return null;
	}

	/**
	 * Using UniProt taxonomy to search organism data of the InParanoid taxonomy identifier list.
	 * Use only to generate code instantiation for all above organisms
	 */
	protected void generateInparanoidOrganisms() throws IOException {
		String url = "http://www.uniprot.org/taxonomy/$id.rdf";

		String inParanoidTaxIDListUrl = "http://inparanoid.sbc.su.se/download/8.0_current/sequences/species.inparanoid8";
		URL u = new URL(inParanoidTaxIDListUrl);
		HttpURLConnection connection = (HttpURLConnection) u.openConnection();

		ArrayList<Integer> failed = new ArrayList<Integer>();

		if (connection.getResponseCode() == 200) {
			BufferedReader input;

			input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			Pattern compile = Pattern.compile("(\\d+)\\.fasta");

			String line = null;
			while ((line = input.readLine()) != null) {
				Matcher matcher = compile.matcher(line);

				if (matcher.find()) {
					Integer id = Integer.valueOf(matcher.group(1));
					//System.out.println(id);

					try {
						Connection c = Jsoup.connect(url.replace("$id", id.toString()))
							.ignoreContentType(true);

						Document document = c.get();

						String scientificName = document.select("scientificName").text();
						String mnemonic = document.select("mnemonic").text();
						String commonName = document.select("commonName").text();

						Organism org = new Organism(scientificName, mnemonic, commonName, id);

						System.out.println("new Organism(\"" + scientificName + "\", \"" + mnemonic + "\", \"" + commonName + "\", " + id + "),");

						/*System.out.println("[");
						System.out.println("\t" + org.getScientificName());
						System.out.println("\t" + org.getGenus());
						System.out.println("\t" + org.getSpecies());
						System.out.println("\t" + org.getAbbrName());
						System.out.println("]");*/
					} catch (Exception e) {
						failed.add(id);
						//e.printStackTrace();
					}
				}
			}

			input.close();

		}

		System.out.println();
		System.out.println("Failed organisms: " + failed);
	}

	public Organism getOrganismByTaxId(int taxId) {
		for (Organism org : organisms)
			if (org.getTaxId() == taxId)
				return org;
		return null;
	}
}
