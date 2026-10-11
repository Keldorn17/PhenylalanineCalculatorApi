-- liquibase formatted sql

-- changeset Patai Zoltan:add-role-user
-- Adds ROLE-USER to Role
-- rollback DELETE FROM roles;
INSERT IGNORE INTO roles (name)
VALUES ('ROLE_USER');

-- changeset Patai Zoltan:add-default-user
-- Adds default user to User
-- rollback DELETE FROM users;
INSERT IGNORE INTO users (user_id, username, email, password)
VALUES (1, '${default_username}', '${default_email}', '${default_password_hash}');

-- changeset Patai Zoltan:add-default-user-to-user-roles
-- Adds default user to UserRoles
-- rollback DELETE FROM user_roles;
INSERT IGNORE INTO user_roles (user_id, role_id)
VALUES (1, 1);

-- changeset Patai Zoltan:add-default-food-types
-- Adds default food types to FoodType
-- rollback DELETE FROM food_type;
INSERT INTO food_type (food_type_id, name, multiplier, user_id, is_deleted)
VALUES (1, 'Gyümölcsök', 30, 1, false),
       (2, 'Zöldségek', 40, 1, false),
       (3, 'Gabonafélék és pékáruk', 50, 1, false),
       (4, 'Tej és tejtermékek', 50, 1, false),
       (5, 'Húsok és baromfik', 50, 1, false),
       (6, 'Halak és tenger gyümölcsei', 50, 1, false),
       (7, 'Tojás', 50, 1, false),
       (8, 'Hüvelyesek', 45, 1, false),
       (9, 'Diófélék és olajos magvak', 50, 1, false),
       (10, 'Zsírok és olajok', 50, 1, false),
       (11, 'Édességek', 50, 1, false),
       (12, 'Italok', 50, 1, false);

-- changeset Patai Zoltan:add-default-food
-- Adds default food to Food
-- rollback DELETE FROM food;
INSERT INTO food (name, protein, calories, user_id, food_type_id, phenylalanine, is_deleted)
VALUES ('Alma (nyers, héjával)', 0.26, 52.00, 1, 1, 7.00, false),
       ('Körte (nyers)', 0.36, 57.00, 1, 1, 11.00, false),
       ('Banán (nyers)', 1.09, 89.00, 1, 1, 49.00, false),
       ('Narancs (nyers)', 0.94, 47.00, 1, 1, 31.00, false),
       ('Citrom (nyers, héj nélkül)', 1.10, 29.00, 1, 1, 29.00, false),
       ('Eper (nyers)', 0.67, 32.00, 1, 1, 26.00, false),
       ('Málna (nyers)', 1.20, 52.00, 1, 1, 41.00, false),
       ('Őszibarack (nyers)', 0.91, 39.00, 1, 1, 34.00, false),
       ('Sárgabarack (nyers)', 1.40, 48.00, 1, 1, 42.00, false),
       ('Szilva (nyers)', 0.70, 46.00, 1, 1, 23.00, false),
       ('Görögdinnye (nyers)', 0.61, 30.00, 1, 1, 16.00, false),
       ('Sárgadinnye (nyers)', 0.84, 34.00, 1, 1, 25.00, false),
       ('Szőlő (nyers)', 0.72, 69.00, 1, 1, 22.00, false),
       ('Kivi (nyers)', 1.14, 61.00, 1, 1, 38.00, false),
       ('Ananász (nyers)', 0.54, 50.00, 1, 1, 18.00, false),
       ('Meggy (nyers)', 1.00, 50.00, 1, 1, 30.00, false),
       ('Cseresznye (nyers)', 1.06, 63.00, 1, 1, 33.00, false),
       ('Áfonya (kék áfonya, nyers)', 0.74, 57.00, 1, 1, 26.00, false),
       ('Grapefruit (nyers, rózsaszín/piros)', 0.77, 42.00, 1, 1, 13.00, false),
       ('Avokádó (nyers)', 2.00, 160.00, 1, 1, 97.00, false),
       ('Aszalt szilva', 2.18, 240.00, 1, 1, 52.00, false),
       ('Datolya (medjool)', 1.81, 277.00, 1, 1, 48.00, false),
       ('Füge (nyers)', 0.75, 74.00, 1, 1, 18.00, false),

       ('Burgonya (nyers, héj nélkül)', 2.05, 77.00, 1, 2, 92.00, false),
       ('Sárgarépa (nyers)', 0.93, 41.00, 1, 2, 38.00, false),
       ('Fejes káposzta (nyers)', 1.28, 25.00, 1, 2, 39.00, false),
       ('Kelbimbó (nyers)', 3.38, 43.00, 1, 2, 137.00, false),
       ('Karfiol (nyers)', 1.92, 25.00, 1, 2, 67.00, false),
       ('Brokkoli (nyers)', 2.82, 34.00, 1, 2, 116.00, false),
       ('Paradicsom (nyers)', 0.88, 18.00, 1, 2, 28.00, false),
       ('Paprika (zöld, nyers)', 0.86, 20.00, 1, 2, 31.00, false),
       ('Paprika (piros, kaliforniai, nyers)', 0.99, 31.00, 1, 2, 35.00, false),
       ('Uborka (nyers, hámozva)', 0.59, 12.00, 1, 2, 20.00, false),
       ('Vöröshagyma (nyers)', 1.10, 40.00, 1, 2, 34.00, false),
       ('Fokhagyma (nyers)', 6.36, 149.00, 1, 2, 183.00, false),
       ('Főzőtök / Cukkini (nyers)', 1.21, 17.00, 1, 2, 45.00, false),
       ('Padlizsán (nyers)', 0.98, 25.00, 1, 2, 48.00, false),
       ('Spenót (nyers)', 2.86, 23.00, 1, 2, 141.00, false),
       ('Fejes saláta (nyers)', 1.36, 15.00, 1, 2, 53.00, false),
       ('Cékla (nyers)', 1.61, 43.00, 1, 2, 60.00, false),
       ('Kerti retek (nyers)', 0.68, 16.00, 1, 2, 22.00, false),
       ('Csiperkegomba (fehér, nyers)', 3.09, 22.00, 1, 2, 114.00, false),
       ('Kukorica (csemege, sárga, nyers)', 3.22, 86.00, 1, 2, 142.00, false),
       ('Zeller (szárzeller, nyers)', 0.69, 14.00, 1, 2, 20.00, false),
       ('Petrezselyemzöld (friss)', 2.97, 36.00, 1, 2, 145.00, false),
       ('Póréhagyma (nyers)', 1.50, 61.00, 1, 2, 55.00, false),
       ('Jégsaláta (nyers)', 0.90, 14.00, 1, 2, 23.00, false),
       ('Római saláta (nyers)', 1.23, 17.00, 1, 2, 65.00, false),
       ('Spárga (nyers)', 2.20, 20.00, 1, 2, 75.00, false),
       ('Sütőtök (nyers)', 1.00, 26.00, 1, 2, 32.00, false),
       ('Cukkini (nyers, héjával)', 1.21, 17.00, 1, 2, 43.00, false),
       ('Vöröskáposzta (nyers)', 1.43, 31.00, 1, 2, 36.00, false),
       ('Zöldbab (nyers)', 1.83, 31.00, 1, 2, 67.00, false),
       ('Csiperkegomba (barna / csiperke, nyers)', 2.50, 22.00, 1, 2, 97.00, false),

       ('Búzaliszt (finomliszt, BL55)', 10.33, 364.00, 1, 3, 546.00, false),
       ('Búzaliszt (teljes kiőrlésű)', 13.70, 340.00, 1, 3, 672.00, false),
       ('Fehér kenyér', 8.85, 266.00, 1, 3, 473.00, false),
       ('Barna / teljes kiőrlésű búza kenyér', 12.45, 252.00, 1, 3, 642.00, false),
       ('Rozskenyér', 8.50, 259.00, 1, 3, 394.00, false),
       ('Fehér rizs (száraz, közepes szemű)', 6.61, 360.00, 1, 3, 347.00, false),
       ('Barna rizs (száraz, hosszú szemű)', 7.54, 367.00, 1, 3, 388.00, false),
       ('Száraztészta (durum, főzetlen)', 13.04, 371.00, 1, 3, 666.00, false),
       ('Zabpehely (nyers)', 16.89, 389.00, 1, 3, 895.00, false),
       ('Kukoricadara', 8.12, 362.00, 1, 3, 401.00, false),
       ('Kuszkusz (száraz)', 12.76, 376.00, 1, 3, 645.00, false),
       ('Árpagyöngy / Gersli (száraz)', 9.91, 352.00, 1, 3, 556.00, false),
       ('Köles (száraz)', 11.02, 378.00, 1, 3, 580.00, false),
       ('Hajdina (száraz, hajdinaliszt)', 12.62, 335.00, 1, 3, 495.00, false),
       ('Pitakenyér (fehér)', 9.10, 275.00, 1, 3, 446.00, false),

       ('Tehéntej (teljes, 3.25% zsírtartalom)', 3.15, 61.00, 1, 4, 159.00, false),
       ('Tehéntej (félzsíros, 1.5% - 2% zsírtartalom)', 3.30, 50.00, 1, 4, 163.00, false),
       ('Natúr joghurt (egész tejből)', 3.47, 61.00, 1, 4, 172.00, false),
       ('Tehéntúró (félzsíros)', 11.12, 98.00, 1, 4, 538.00, false),
       ('Trappista / Gouda sajt', 24.94, 356.00, 1, 4, 1342.00, false),
       ('Cheddar sajt', 24.90, 403.00, 1, 4, 1310.00, false),
       ('Mozzarella sajt (teljes tejből)', 22.17, 300.00, 1, 4, 1148.00, false),
       ('Tejföl (kb. 20% zsírtartalom)', 2.44, 198.00, 1, 4, 115.00, false),
       ('Vaj (sótlan)', 0.85, 717.00, 1, 4, 38.00, false),
       ('Parmezán sajt (reszelt)', 28.42, 420.00, 1, 4, 1538.00, false),
       ('Feta sajt', 14.21, 265.00, 1, 4, 675.00, false),
       ('Gouda sajt', 24.94, 356.00, 1, 4, 1431.00, false),
       ('Habtejszín (36% zsírtartalom)', 2.84, 340.00, 1, 4, 130.00, false),

       ('Egész tojás (friss, nyers)', 12.56, 143.00, 1, 7, 680.00, false),
       ('Tojásfehérje (friss, nyers)', 10.90, 52.00, 1, 7, 655.00, false),
       ('Tojássárgája (friss, nyers)', 15.86, 322.00, 1, 7, 687.00, false),

       ('Csirkemellfilé (nyers, bőr nélkül)', 22.50, 120.00, 1, 5, 897.00, false),
       ('Csirkecomb (felsőcomb, nyers, bőr nélkül)', 19.73, 121.00, 1, 5, 778.00, false),
       ('Pulykamell (nyers)', 23.66, 111.00, 1, 5, 915.00, false),
       ('Sertéstarja (nyers)', 17.51, 234.00, 1, 5, 725.00, false),
       ('Sertéskaraj (nyers, sovány)', 21.68, 143.00, 1, 5, 874.00, false),
       ('Marhalapocka / marhahús (sovány, nyers)', 21.80, 146.00, 1, 5, 883.00, false),
       ('Marhacomb (sovány, nyers)', 22.31, 141.00, 1, 5, 905.00, false),
       ('Darált marhahús (80% hús / 20% zsír, nyers)', 17.17, 254.00, 1, 5, 670.00, false),
       ('Kacsahús (hús és bőr, nyers)', 11.49, 404.00, 1, 5, 459.00, false),
       ('Pulykacomb (hús és bőr, nyers)', 19.54, 144.00, 1, 5, 769.00, false),
       ('Bárányhús (darált, nyers)', 16.56, 282.00, 1, 5, 674.00, false),

       ('Ponty (nyers)', 17.83, 127.00, 1, 6, 699.00, false),
       ('Lazac (atlanti, nyers)', 20.42, 208.00, 1, 6, 796.00, false),
       ('Tonhal (sárgaúszójú, nyers)', 24.40, 109.00, 1, 6, 968.00, false),
       ('Tőkehal (atlanti, nyers)', 17.81, 82.00, 1, 6, 696.00, false),
       ('Garnélarák (nyers)', 13.61, 71.00, 1, 6, 593.00, false),
       ('Pisztráng (sült)', 26.63, 190.00, 1, 6, 1040.00, false),
       ('Hering (atlanti, nyers)', 17.96, 158.00, 1, 6, 701.00, false),

       ('Lencse (száraz)', 25.80, 353.00, 1, 8, 1319.00, false),
       ('Szárazbab (fehér, nyers)', 23.36, 337.00, 1, 8, 1243.00, false),
       ('Csicseriborsó (száraz)', 19.30, 364.00, 1, 8, 1070.00, false),
       ('Sárgaborsó / Zöldborsó (száraz)', 24.55, 341.00, 1, 8, 1118.00, false),
       ('Zöldborsó (friss, nyers)', 5.42, 81.00, 1, 2, 239.00, false),
       ('Szójabab (száraz mag, nyers)', 36.49, 446.00, 1, 8, 2122.00, false),
       ('Vörösbab / Kidney bab (száraz, nyers)', 22.53, 337.00, 1, 8, 1218.00, false),

       ('Dió (szárított)', 15.23, 654.00, 1, 9, 686.00, false),
       ('Mogyoró (földimogyoró, nyers)', 25.80, 567.00, 1, 9, 1377.00, false),
       ('Mandula', 21.15, 579.00, 1, 9, 1120.00, false),
       ('Napraforgómag (hántolt, szárított)', 20.78, 584.00, 1, 9, 932.00, false),
       ('Tökmag (szárított)', 30.23, 559.00, 1, 9, 1546.00, false),
       ('Törökmogyoró (nyers)', 14.95, 628.00, 1, 9, 663.00, false),
       ('Kesudió (nyers)', 18.22, 553.00, 1, 9, 951.00, false),
       ('Pisztácia (nyers)', 20.16, 560.00, 1, 9, 1092.00, false),
       ('Szezámmag (hántolt, szárított)', 20.45, 631.00, 1, 9, 940.00, false),
       ('Mák (szárított)', 17.99, 525.00, 1, 9, 758.00, false),

       ('Napraforgó étolaj', 0.00, 884.00, 1, 10, 0.00, false),
       ('Olívaolaj', 0.00, 884.00, 1, 10, 0.00, false),
       ('Sertészsír', 0.00, 902.00, 1, 10, 0.00, false),

       ('Kristálycukor', 0.00, 387.00, 1, 11, 0.00, false),
       ('Méz', 0.30, 304.00, 1, 11, 8.00, false),
       ('Étcsokoládé (70-85% kakaó)', 7.79, 598.00, 1, 11, 382.00, false),
       ('Tejcsokoládé', 7.65, 535.00, 1, 11, 365.00, false),

       ('Feketekávé (főzött, csapvízzel)', 0.12, 1.00, 1, 12, 3.00, false),
       ('Fekete tea (főzött, filteres/leveles)', 0.00, 1.00, 1, 12, 0.00, false),
       ('Narancslé (100%-os, friss)', 0.70, 45.00, 1, 12, 9.00, false);