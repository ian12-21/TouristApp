package com.touristapp.ui.screens.places

import com.touristapp.data.model.Place

/**
 * Dummy data for design iteration. Remove once real Firestore data is flowing.
 */
val dummyPlaces: List<Place> = listOf(
    // ── Beaches ──
    Place(
        id = "b1", name = "Zlatni Rat", category = "beach",
        description = "Iconic golden horn beach with crystal-clear water and pine tree shade.",
        address = "Bol, Brac Island",
        photoUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400",
        rating = 4.8, tips = "Arrive early to grab a spot near the tip.",
        website = "https://visitbol.com", phone = "+385 21 635 638"
    ),
    Place(
        id = "b2", name = "Banje Beach", category = "beach",
        description = "Popular pebble beach right outside Dubrovnik's Old Town walls.",
        address = "Frana Supila, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1519046904884-53103b34b206?w=400",
        rating = 4.5, tips = "Great sunset views of the city walls.",
        website = "", phone = "+385 20 311 500"
    ),
    Place(
        id = "b3", name = "Stiniva Cove", category = "beach",
        description = "Hidden cove accessible by boat or a steep trail, voted Europe's best beach.",
        address = "Vis Island",
        photoUrl = "https://images.unsplash.com/photo-1473116763249-2faaef81ccda?w=400",
        rating = 4.9, tips = "Bring water shoes for the rocky entrance.",
        website = "", phone = ""
    ),
    Place(
        id = "b4", name = "Sakarun Beach", category = "beach",
        description = "Sandy paradise with shallow turquoise water, perfect for families.",
        address = "Dugi Otok Island",
        photoUrl = "https://images.unsplash.com/photo-1504681869696-d977211a5f4c?w=400",
        rating = 4.6, tips = "Parking fills up fast in summer.",
        website = "", phone = ""
    ),
    Place(
        id = "b5", name = "Bacvice Beach", category = "beach",
        description = "Sandy city beach famous for the traditional picigin ball game.",
        address = "Split",
        photoUrl = "https://images.unsplash.com/photo-1506953823976-52e1fdc0149a?w=400",
        rating = 4.3, tips = "Join the locals for a game of picigin.",
        website = "", phone = ""
    ),
    Place(
        id = "b6", name = "Rajska Plaza", category = "beach",
        description = "Paradise beach with fine sand stretching over 1.5 km on Rab Island.",
        address = "Lopar, Rab Island",
        photoUrl = "https://images.unsplash.com/photo-1520942702018-0862200e6873?w=400",
        rating = 4.7, tips = "Great for kids due to shallow waters.",
        website = "", phone = ""
    ),
    Place(
        id = "b7", name = "Pakleni Islands", category = "beach",
        description = "Secluded island archipelago with hidden bays and gin-clear water.",
        address = "Pakleni Islands, Hvar",
        photoUrl = "https://images.unsplash.com/photo-1471922694854-ff1b63b20054?w=400",
        rating = 4.8, tips = "Rent a small boat from Hvar harbour to island-hop.",
        website = "", phone = ""
    ),

    // ── Restaurants ──
    Place(
        id = "r1", name = "Konoba Matejuska", category = "restaurant",
        description = "Tiny family-run tavern serving the freshest seafood in the heart of Split.",
        address = "Tomica Stine 3, Split",
        photoUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400",
        rating = 4.7, tips = "Try the octopus salad — it's legendary.",
        website = "", phone = "+385 21 355 152"
    ),
    Place(
        id = "r2", name = "Nautika", category = "restaurant",
        description = "Upscale fine dining with stunning views of Dubrovnik's walls and Lovrijenac.",
        address = "Brsalje 3, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400",
        rating = 4.6, tips = "Reserve a terrace table for sunset.",
        website = "https://nautikarestaurant.com", phone = "+385 20 442 526"
    ),
    Place(
        id = "r3", name = "Pelegrini", category = "restaurant",
        description = "Michelin-starred restaurant set inside medieval Sibenik walls.",
        address = "Jurja Dalmatinca 1, Sibenik",
        photoUrl = "https://images.unsplash.com/photo-1559339352-11d035aa65de?w=400",
        rating = 4.9, tips = "Book weeks in advance during summer.",
        website = "https://pelegrini.hr", phone = "+385 22 213 701"
    ),
    Place(
        id = "r4", name = "Konoba Batelina", category = "restaurant",
        description = "No-menu seafood spot where the chef decides based on the day's catch.",
        address = "Cimulje 25, Banjole",
        photoUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=400",
        rating = 4.8, tips = "Just trust the chef and enjoy the ride.",
        website = "", phone = "+385 52 573 767"
    ),
    Place(
        id = "r5", name = "Zinfandel's", category = "restaurant",
        description = "Elegant restaurant in the Esplanade hotel, a Zagreb institution since 1925.",
        address = "Mihanoviceva 1, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1550966871-3ed3cdb51f3a?w=400",
        rating = 4.5, tips = "The struklji dessert is a must.",
        website = "https://esplanade.hr", phone = "+385 1 456 6666"
    ),
    Place(
        id = "r6", name = "Konoba Stori Komin", category = "restaurant",
        description = "Rustic Dalmatian cooking over an open hearth in a stone house.",
        address = "Komiski zaljev, Vis",
        photoUrl = "https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?w=400",
        rating = 4.4, tips = "The slow-cooked lamb peka is incredible.",
        website = "", phone = "+385 21 713 175"
    ),
    Place(
        id = "r7", name = "Bokeria Kitchen & Wine", category = "restaurant",
        description = "Modern Mediterranean tapas and natural wines in a lively Split side street.",
        address = "Domaldova 8, Split",
        photoUrl = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400",
        rating = 4.6, tips = "Go for the tuna tartare and a glass of Posip.",
        website = "", phone = "+385 21 355 577"
    ),

    // ── Places (museums, historic sites) ──
    Place(
        id = "p1", name = "Diocletian's Palace", category = "place",
        description = "Ancient Roman palace built in the 4th century, now the living heart of Split.",
        address = "Dioklecijanova ul., Split",
        photoUrl = "https://images.unsplash.com/photo-1555990793-da11153b2473?w=400",
        rating = 4.8, tips = "Visit the basement halls to see the original layout.",
        website = "https://visitsplit.com", phone = ""
    ),
    Place(
        id = "p2", name = "Plitvice Lakes", category = "place",
        description = "UNESCO World Heritage site with 16 terraced lakes and stunning waterfalls.",
        address = "Plitvicka Jezera",
        photoUrl = "https://images.unsplash.com/photo-1504214208698-ea1916a2195a?w=400",
        rating = 4.9, tips = "Take the upper lakes route for fewer crowds.",
        website = "https://np-plitvicka-jezera.hr", phone = "+385 53 751 015"
    ),
    Place(
        id = "p3", name = "Museum of Broken Relationships", category = "place",
        description = "Unique museum displaying personal objects from failed relationships worldwide.",
        address = "Cirilometodska 2, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1566127444979-b3d2b654e3d7?w=400",
        rating = 4.6, tips = "Allow at least 1.5 hours to read all the stories.",
        website = "https://brokenships.com", phone = "+385 1 485 1021"
    ),
    Place(
        id = "p4", name = "Arena Pula", category = "place",
        description = "Remarkably preserved Roman amphitheatre, one of the largest in the world.",
        address = "Flavijevska ul., Pula",
        photoUrl = "https://images.unsplash.com/photo-1558618666-fcd25c85f82e?w=400",
        rating = 4.7, tips = "Check for summer concert schedules — the acoustics are amazing.",
        website = "", phone = "+385 52 219 028"
    ),
    Place(
        id = "p5", name = "Dubrovnik City Walls", category = "place",
        description = "Walk the full 2 km circuit atop the medieval walls for panoramic Adriatic views.",
        address = "Placa ul., Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1555990538-1e2a0a37f260?w=400",
        rating = 4.8, tips = "Go early morning or late afternoon to avoid heat and crowds.",
        website = "https://wallsofdubrovnik.com", phone = "+385 20 638 800"
    ),
    Place(
        id = "p6", name = "Krka National Park", category = "place",
        description = "Stunning river canyon with travertine waterfalls where you can swim.",
        address = "Lozovac, Sibenik",
        photoUrl = "https://images.unsplash.com/photo-1500259571355-332da5cb07aa?w=400",
        rating = 4.7, tips = "Swim at the base of Skradinski Buk waterfall.",
        website = "https://npkrka.hr", phone = "+385 22 201 777"
    ),
    Place(
        id = "p7", name = "Trogir Old Town", category = "place",
        description = "UNESCO-listed medieval island town with Romanesque churches and narrow alleys.",
        address = "Trogir",
        photoUrl = "https://images.unsplash.com/photo-1580137189272-c9379f8864fd?w=400",
        rating = 4.6, tips = "Climb the Kamerlengo fortress tower for a 360° panorama.",
        website = "", phone = ""
    ),

    // ── Drinks & Coffee ──
    Place(
        id = "c1", name = "Cogito Coffee", category = "cafe",
        description = "Specialty third-wave coffee roasters with several cozy Split locations.",
        address = "Ivanica 2, Split",
        photoUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400",
        rating = 4.7, tips = "Ask for their single-origin pour-over.",
        website = "https://cogitocoffee.com", phone = ""
    ),
    Place(
        id = "c2", name = "D16 Coffee", category = "cafe",
        description = "Minimalist specialty cafe hidden in a courtyard off Dubrovnik's main street.",
        address = "Od Puca 16, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=400",
        rating = 4.6, tips = "Perfect espresso stop between sightseeing.",
        website = "", phone = ""
    ),
    Place(
        id = "c3", name = "Eli's Cafe", category = "cafe",
        description = "Charming terrace cafe on the Riva promenade, ideal for people-watching.",
        address = "Obala HNP 6, Split",
        photoUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=400",
        rating = 4.4, tips = "Great spot for morning coffee with a sea view.",
        website = "", phone = "+385 21 345 678"
    ),
    Place(
        id = "c4", name = "Johan Franck", category = "cafe",
        description = "Grand Art Nouveau cafe in Zagreb's central square, a city landmark since 1892.",
        address = "Trg bana Jelacica 9, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1559305616-3f99cd43e353?w=400",
        rating = 4.5, tips = "Try their signature Franck blend.",
        website = "", phone = "+385 1 481 5192"
    ),
    Place(
        id = "c5", name = "Buzz Bar", category = "cafe",
        description = "Craft cocktail bar with inventive drinks using local Croatian ingredients.",
        address = "Marmontova 1, Split",
        photoUrl = "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=400",
        rating = 4.6, tips = "The fig and rakija cocktail is a local favorite.",
        website = "", phone = ""
    ),
    Place(
        id = "c6", name = "Lemon Bar", category = "cafe",
        description = "Bright rooftop bar with fresh cocktails overlooking the old town rooftops.",
        address = "Maruliceva 2, Split",
        photoUrl = "https://images.unsplash.com/photo-1551024709-8f23befc6f87?w=400",
        rating = 4.3, tips = "Come for sunset — the view is unbeatable.",
        website = "", phone = ""
    ),
    Place(
        id = "c7", name = "Mala Buza", category = "cafe",
        description = "Cliff-side bar outside Dubrovnik's walls serving cold drinks above the sea.",
        address = "Od Margarite, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1530373239216-42518e63a76b?w=400",
        rating = 4.8, tips = "Look for the 'Cold Drinks' sign in the wall — that's the entrance.",
        website = "", phone = ""
    ),

    // ── Shops ──
    Place(
        id = "s1", name = "Uje Oil Bar", category = "shop",
        description = "Boutique olive oil shop with tastings of premium Croatian extra virgin oils.",
        address = "Dominisova 3, Split",
        photoUrl = "https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?w=400",
        rating = 4.8, tips = "The truffle-infused oil makes a perfect souvenir.",
        website = "https://uje.hr", phone = "+385 21 346 819"
    ),
    Place(
        id = "s2", name = "Croatia Gift Shop", category = "shop",
        description = "Curated collection of authentic Croatian souvenirs and artisan crafts.",
        address = "Placa ul. 12, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=400",
        rating = 4.3, tips = "Look for the hand-painted ceramics.",
        website = "", phone = ""
    ),
    Place(
        id = "s3", name = "Natura Croatica", category = "shop",
        description = "Natural cosmetics and lavender products from Hvar Island farms.",
        address = "Preobrazenska 8, Split",
        photoUrl = "https://images.unsplash.com/photo-1604719312566-8912e9227c6a?w=400",
        rating = 4.5, tips = "The lavender essential oil set is a bestseller.",
        website = "", phone = "+385 21 344 200"
    ),
    Place(
        id = "s4", name = "Dolac Market", category = "shop",
        description = "Zagreb's beloved open-air farmers market with fresh produce and local delicacies.",
        address = "Dolac 9, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1488459716781-31db52582fe9?w=400",
        rating = 4.7, tips = "Go before 10am for the best selection of cheese and cured meats.",
        website = "", phone = ""
    ),
    Place(
        id = "s5", name = "Diocletian's Wine House", category = "shop",
        description = "Wine shop specializing in Croatian wines with daily tasting sessions.",
        address = "Kraj sv. Marije 1, Split",
        photoUrl = "https://images.unsplash.com/photo-1567696911980-2eed69a46042?w=400",
        rating = 4.6, tips = "Ask about Plavac Mali — Croatia's signature red grape.",
        website = "", phone = "+385 21 321 890"
    ),
    Place(
        id = "s6", name = "Think Pink", category = "shop",
        description = "Trendy concept store with Croatian fashion designers and accessories.",
        address = "Ilica 7, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?w=400",
        rating = 4.2, tips = "Great for unique Croatian designer pieces.",
        website = "", phone = ""
    ),
    Place(
        id = "s7", name = "Aromatica Fine Food", category = "shop",
        description = "Gourmet deli stocking Croatian truffles, honey, jams, and artisan chocolates.",
        address = "Obrov 8, Split",
        photoUrl = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=400",
        rating = 4.5, tips = "Their truffle honey is an amazing gift.",
        website = "", phone = "+385 21 348 111"
    ),

    // ── Nightlife ──
    Place(
        id = "n1", name = "Carpe Diem Beach", category = "nightlife",
        description = "Legendary island beach club on Stipanska, accessible by boat from Hvar.",
        address = "Stipanska Island, Hvar",
        photoUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=400",
        rating = 4.7, tips = "Book a daybed in advance for peak season.",
        website = "https://carpediembeach.com", phone = "+385 21 717 233"
    ),
    Place(
        id = "n2", name = "Revelin Club", category = "nightlife",
        description = "World-class nightclub inside a 16th-century fortress in Dubrovnik.",
        address = "Sv. Dominika 3, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1571266028243-e4733b0f0bb0?w=400",
        rating = 4.5, tips = "Check their lineup — top international DJs play here.",
        website = "https://clubrevelin.com", phone = "+385 20 322 164"
    ),
    Place(
        id = "n3", name = "Central Club", category = "nightlife",
        description = "Split's go-to rooftop club with electronic music and harbour views.",
        address = "Kralja Tomislava 1, Split",
        photoUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=400",
        rating = 4.3, tips = "Gets busy after midnight.",
        website = "", phone = ""
    ),
    Place(
        id = "n4", name = "Hula Hula Beach Bar", category = "nightlife",
        description = "Iconic sunset bar on the rocks with live DJs and cocktails.",
        address = "Pokonji Dol, Hvar",
        photoUrl = "https://images.unsplash.com/photo-1510414842594-a61c69b5ae57?w=400",
        rating = 4.6, tips = "Arrive 2 hours before sunset to get a good spot.",
        website = "", phone = ""
    ),
    Place(
        id = "n5", name = "Gallery Club", category = "nightlife",
        description = "Stylish multi-floor club in Zagreb's upper town with art installations.",
        address = "Tkalciceva 10, Zagreb",
        photoUrl = "https://images.unsplash.com/photo-1545128485-c400e7702796?w=400",
        rating = 4.4, tips = "Thursday is the best night to go.",
        website = "", phone = "+385 1 481 1333"
    ),
    Place(
        id = "n6", name = "Baja Bar", category = "nightlife",
        description = "Chill open-air cocktail bar with live music overlooking Bacvice Bay.",
        address = "Bacvice, Split",
        photoUrl = "https://images.unsplash.com/photo-1572116469696-31de0f17cc34?w=400",
        rating = 4.2, tips = "Perfect warm-up spot before heading to the clubs.",
        website = "", phone = ""
    ),
    Place(
        id = "n7", name = "Aurora Club", category = "nightlife",
        description = "Open-air seaside club in Primosten with foam parties and live acts.",
        address = "Huljerat ul., Primosten",
        photoUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400",
        rating = 4.4, tips = "Summer weekends get packed — arrive before 11pm.",
        website = "", phone = "+385 22 571 007"
    ),

    // ── Activities & Entertainment ──
    Place(
        id = "a1", name = "Red Adventures Rafting", category = "activity",
        description = "Thrilling whitewater rafting on the Cetina River through a stunning canyon.",
        address = "Put Radosvica 2, Omis",
        photoUrl = "https://images.unsplash.com/photo-1530866495561-507c83401bff?w=400",
        rating = 4.8, tips = "No experience needed — guides handle everything.",
        website = "https://redadventures.com", phone = "+385 21 863 015"
    ),
    Place(
        id = "a2", name = "Jet Ski Split", category = "activity",
        description = "Jet ski rentals and guided sea tours along the Split coastline and islands.",
        address = "Znjan Beach, Split",
        photoUrl = "https://images.unsplash.com/photo-1601024445121-e5b82f020549?w=400",
        rating = 4.5, tips = "The sunset tour to Ciovo island is worth the extra cost.",
        website = "", phone = "+385 95 123 4567"
    ),
    Place(
        id = "a3", name = "Split Walking Tours", category = "activity",
        description = "Free guided walking tour through Diocletian's Palace and the old town.",
        address = "Peristil Square, Split",
        photoUrl = "https://images.unsplash.com/photo-1569949381669-ecf31ae8e613?w=400",
        rating = 4.7, tips = "Tours start at 10am daily — just show up at Peristil.",
        website = "https://splitwalkingtour.com", phone = ""
    ),
    Place(
        id = "a4", name = "Aquarium Dubrovnik", category = "activity",
        description = "Historic aquarium inside Fort St. John showcasing Adriatic marine life.",
        address = "Kneza Damjana Jude 2, Dubrovnik",
        photoUrl = "https://images.unsplash.com/photo-1544551763-46a013bb70d5?w=400",
        rating = 4.3, tips = "Great rainy-day activity — kids love the touch pool.",
        website = "", phone = "+385 20 323 978"
    ),
    Place(
        id = "a5", name = "Omiš Paragliding", category = "activity",
        description = "Tandem paragliding flights over the Cetina canyon with Adriatic sea views.",
        address = "Omis",
        photoUrl = "https://images.unsplash.com/photo-1503256207526-0d5d80fa2f47?w=400",
        rating = 4.9, tips = "Book morning flights for the calmest thermals.",
        website = "", phone = "+385 91 765 4321"
    ),
    Place(
        id = "a6", name = "CrossFit Split", category = "activity",
        description = "Modern CrossFit box offering drop-in classes for travelers.",
        address = "Osjecka 28, Split",
        photoUrl = "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400",
        rating = 4.4, tips = "Drop-in is 15€ — morning classes are less crowded.",
        website = "", phone = "+385 21 456 789"
    ),
    Place(
        id = "a7", name = "Blue Cave & Hvar Tour", category = "activity",
        description = "Full-day speedboat tour visiting the Blue Cave, Hvar, and Pakleni Islands.",
        address = "Riva Harbour, Split",
        photoUrl = "https://images.unsplash.com/photo-1544735716-392fe2489ffa?w=400",
        rating = 4.8, tips = "Bring sunscreen and a windbreaker for the boat ride.",
        website = "https://bluecavetour.hr", phone = "+385 21 888 999"
    )
)
