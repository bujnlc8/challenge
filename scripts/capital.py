# coding=utf-8

from __future__ import unicode_literals
import random

CAPITALS = [
    (u"\u4e2d\u56fd People's Republic of China", u'\u5317\u4eac Beijing'),
    (u'\u8499\u53e4 Mongolia', u'\u4e4c\u5170\u5df4\u6258 Elggydggmgj'),
    (u"\u671d\u9c9c Democratic People's Republic of Korea", u'\u5e73\u58e4 Pyongyang'),
    (u'\u97e9\u56fd Republic of Korea', u'\u9996\u5c14 Seoul'), (u'\u65e5\u672c Japan', u'\u4e1c\u4eac Tokyo'),
    (u'\u83f2\u5f8b\u5bbe Republic of the Philippines', u'\u9a6c\u5c3c\u62c9 Manila'),
    (u'\u5370\u5ea6\u5c3c\u897f\u4e9a Republic of Indonesia', u'\u96c5\u52a0\u8fbe Jakarta'),
    (u'\u6587\u83b1 Brunei Darussalam', u'\u65af\u91cc\u5df4\u52a0\u6e7e\u5e02 Bandar Seri Begawan'),
    (u'\u65b0\u52a0\u5761 Republic of Singapore', u'\u65b0\u52a0\u5761 Singapore'),
    (u'\u6cf0\u56fd Kingdom of Thailand', u'\u66fc\u8c37 Bangkok'),
    (u'\u9a6c\u6765\u897f\u4e9a Malaysia', u'\u5409\u9686\u5761 Kuala Lumpur'),
    (u'\u8d8a\u5357 Socialist Republic of Vietnam', u'\u6cb3\u5185 Hanoi'),
    (u"\u8001\u631d Lao People's Democratic Republic", u'\u4e07\u8c61 Vientiane'),
    (u'\u67ec\u57d4\u5be8 Kingdom of Cambodia', u'\u91d1\u8fb9 Phnom Penh'),
    (u'\u7f05\u7538 Union of Myanmar', u'\u5185\u6bd4\u90fd Naypyidaw'),
    (u'\u4e0d\u4e39 Kingdom of Bhutan', u'\u5ef7\u5e03 Thimphu'),
    (u'\u4e1c\u5e1d\u6c76 Democratic Republic of East Timor', u'\u5e1d\u529b Dili'),
    (u'\u5c3c\u6cca\u5c14 Kingdom of Nepal', u'\u52a0\u5fb7\u6ee1\u90fd Kathmandu'),
    (u'\u5370\u5ea6 Republic of India', u'\u65b0\u5fb7\u91cc New Delhi'),
    (u"\u5b5f\u52a0\u62c9\u56fd People's Republic of Bangladesh", u'\u8fbe\u5361 Dhaka'),
    (u'\u65af\u91cc\u5170\u5361 Democratic Socialist Republic of Sri Lanka', u'\u79d1\u4f26\u5761 Colombo'),
    (u'\u9a6c\u5c14\u4ee3\u592b Republic of Maldives', u'\u9a6c\u7d2f Male'),
    (u'\u5df4\u57fa\u65af\u5766 Islamic Republic of Pakistan', u'\u4f0a\u65af\u5170\u5821 Islamabad'),
    (u'\u963f\u5bcc\u6c57 Islamic State of Afghanistan', u'\u5580\u5e03\u5c14 Kabul'),
    (u'\u5854\u5409\u514b\u65af\u5766 Republic of Tajikistan', u'\u675c\u5c1a\u522b Dushanbe'),
    (u'\u5409\u5c14\u5409\u65af\u65af\u5766 Kyrgyz Republic', u'\u6bd4\u4ec0\u51ef\u514b Bishkek'),
    (u'\u54c8\u8428\u514b\u65af\u5766 Republic of Kazakhstan', u'\u963f\u65af\u5854\u7eb3 Astana'),
    (u'\u4e4c\u5179\u522b\u514b\u671f\u5766 Republic of Uzbekistan', u'\u5854\u4ec0\u5e72 Tashkent'),
    (u'\u571f\u5e93\u66fc\u65af\u5766 Republic of Turkmenistan', u'\u963f\u4ec0\u54c8\u5df4\u5fb7 Ashgabat'),
    (u'\u4f0a\u6717 Islamic Republic of Iran', u'\u5fb7\u9ed1\u5170 Tehran'),
    (u'\u4f0a\u62c9\u514b Republic of Iraq', u'\u5df4\u683c\u8fbe Baghdad'),
    (u'\u79d1\u5a01\u7279 State of Kuwait', u'\u79d1\u5a01\u7279\u57ce Kuwait'),
    (u'\u5361\u5854\u5c14 State of Qatarc', u'\u591a\u54c8 Doha'),
    (u'\u963f\u62c9\u4f2f\u8054\u5408\u914b\u957f\u56fd United Arab Emirates', u'\u963f\u5e03\u624e\u6bd4 Abu Dhabi'),
    (u'\u5df4\u6797 Kingdom of Bahrain', u'\u9ea6\u7eb3\u9ea6 Manama'),
    (u'\u963f\u66fc Sultanate of Oman', u'\u9a6c\u65af\u5580\u7279 Muscat'),
    (u'\u4e5f\u95e8 Republic of Yemen', u'\u8428\u90a3 Sanaa'),
    (u'\u6c99\u7279\u963f\u62c9\u4f2f Kingdom of Saudi Arabia', u'\u5229\u96c5\u5f97 Riyadh'),
    (u'\u7ea6\u65e6 Hashemite Kingdom of Jordan', u'\u5b89\u66fc Amman'),
    (u'\u5df4\u52d2\u65af\u5766 State of Palestine', u'\u8036\u8def\u6492\u51b7 Jerusalem'),
    (u'\u4ee5\u8272\u5217 State of Israel', u'\u8036\u8def\u6492\u51b7 Jerusalem'),
    (u'\u53d9\u5229\u4e9a Syrian Arab Republic', u'\u5927\u9a6c\u58eb\u9769 Damascus'),
    (u'\u9ece\u5df4\u5ae9 Lebanese Republic', u'\u8d1d\u9c81\u7279 Beirut'),
    (u'\u585e\u6d66\u8def\u65af Republic of Cyprus', u'\u5c3c\u79d1\u897f\u4e9a Nicosia'),
    (u'\u571f\u8033\u5176 Republic of Turkey', u'\u5b89\u5361\u62c9 Ankara'),
    (u'\u963f\u585e\u62dc\u7586 Republic of Azerbaijan', u'\u5df4\u5e93 Baku'),
    (u'\u683c\u9c81\u5409\u4e9a Georgia', u"\u7b2c\u6bd4\u5229\u65af T'bilisi"),
    (u'\u4e9a\u7f8e\u5c3c\u4e9a Republic of Armenia', u'\u57c3\u91cc\u6e29 Yerevan'),
    (u'\u632a\u5a01 Kingdom of Norway', u'\u5965\u65af\u8def Oslo'),
    (u'\u51b0\u5c9b Republic of Iceland', u'\u96f7\u514b\u96c5\u672a\u514b Reykjavik'),
    (u'\u745e\u5178 Kingdom of Sweden', u'\u65af\u5fb7\u54e5\u5c14\u6469 Stockholm'),
    (u'\u82ac\u5170 Republic of Finland', u'\u8d6b\u5c14\u8f9b\u57fa Helsinki'),
    (u'\u7231\u6c99\u5c3c\u4e9a Republic of Estonia', u'\u5854\u6797 Tallinn'),
    (u'\u62c9\u8131\u7ef4\u4e9a Republic of Latvia', u'\u91cc\u52a0 Riga'),
    (u'\u7acb\u9676\u5b9b Republic of Lithuania', u'\u7ef4\u5c14\u7ebd\u65af Vilnius'),
    (u'\u767d\u4fc4\u7f57\u65af Republic of Belarus', u'\u660e\u65af\u514b Minsk'),
    (u'\u4fc4\u7f57\u65af Russian Federation', u'\u83ab\u65af\u79d1 Moscow'),
    (u'\u4e4c\u514b\u5170 Ukraine', u'\u57fa\u8f85 Kiev'),
    (u'\u6469\u5c14\u591a\u74e6 Republic of Moldova', u'\u57fa\u5e0c\u8bb7\u4e4c Chisinau'),
    (u'\u6ce2\u5170 Republic of Poland', u'\u534e\u6c99 Warsaw'),
    (u'\u6377\u514b Czech Republic', u'\u5e03\u62c9\u683cPrague'),
    (u'\u65af\u6d1b\u4f10\u514b Slovak Republic', u'\u5e03\u62c9\u63d0\u65af\u62c9\u53d1 Bratislava'),
    (u'\u5308\u7259\u5229 Republic of Hungary', u'\u5e03\u8fbe\u4f69\u65af Budapest'),
    (u'\u5fb7\u56fd Federal Republic of Germany', u'\u67cf\u6797 Berlin'),
    (u'\u82f1\u56fd United kingdom of Great Britain and Northern Ireland', u'\u4f26\u6566 London'),
    (u'\u7231\u5c14\u5170 Ireland', u'\u90fd\u67cf\u6797 Dublin'),
    (u'\u4e39\u9ea6 Kingdom of Denmark', u'\u54e5\u672c\u54c8\u6839 Copenhagen'),
    (u'\u8377\u5170 Kingdom of the Netherlands', u'\u963f\u59c6\u65af\u7279\u4e39 Amsterdam'),
    (u'\u6469\u7eb3\u54e5 Principality of Monaco', u'\u6469\u7eb3\u54e5 Monaco'),
    (u'\u6cd5\u56fd French Republic', u'\u5df4\u9ece Paris'),
    (u'\u6bd4\u5229\u65f6 Kingdom of Belgium', u'\u5e03\u9c81\u585e\u5c14 Brussels'),
    (u'\u5362\u68ee\u5821 Grand Duchy of Luxembourg', u'\u5362\u68ee\u5821 Luxembourg'),
    (u'\u5965\u5730\u5229 Republic of Austria', u'\u7ef4\u4e5f\u7eb3 Vienna'),
    (u'\u745e\u58eb Swiss Confederation', u'\u4f2f\u5c14\u5c3c Bern'),
    (u'\u5217\u652f\u6566\u58eb\u767b Principality of Liechtenstein', u'\u74e6\u675c\u5179 Vaduz'),
    (u'\u897f\u73ed\u7259 Kingdom of Spain', u'\u9a6c\u5fb7\u91cc Madrid'),
    (u'\u5b89\u9053\u5c14 Principality of Andorra', u'\u5b89\u9053\u5c14 Andorra la Vella'),
    (u'\u8461\u8404\u7259 Portuguese Republic', u'\u91cc\u65af\u672c Lisbon'),
    (u'\u610f\u5927\u5229 Italian Republic', u'\u7f57\u9a6c Rome'),
    (u'\u9a6c\u8033\u4ed6 Republic of Malta', u'\u74e6\u83b1\u5854 Bamako'),
    (u'\u5723\u9a6c\u529b\u8bfa Republic of San Marino', u'\u5723\u9a6c\u529b\u8bfa San Marino'),
    (u'\u68b5\u8482\u5188 Vatican City State', u'\u68b5\u8482\u5188\u57ce Vatican City'),
    (u'\u65af\u6d1b\u6587\u5c3c\u4e9a Republic of Slovenia', u'\u5362\u5e03\u5c14\u96c5\u90a3 Ljubljana'),
    (u'\u514b\u7f57\u5730\u4e9a Republic of Croatia', u'\u8428\u683c\u52d2\u5e03 Zagreb'),
    (
        u'\u6ce2\u65af\u5c3c\u4e9a\u548c\u9ed1\u585e\u54e5\u7ef4\u90a3 Bosnia and Herzegovina',
        u'\u8428\u62c9\u70ed\u7a9d Sarajevo'
    ), (u'\u5357\u65af\u62c9\u592b Federal Republic of Yugoslavia', u'\u8d1d\u5c14\u683c\u83b1\u5fb7 Belgrade'),
    (u'\u5317\u9a6c\u5176\u987f The Republic of North Macedonia', u'\u65af\u79d1\u666e\u91cc Skopje'),
    (u'\u963f\u5c14\u5df4\u5c3c\u4e9a Republic of Albania', u'\u5730\u62c9\u90a3 Tirana'),
    (u'\u7f57\u9a6c\u5c3c\u4e9a Romania', u'\u5e03\u52a0\u52d2\u65af\u7279 Bucharest'),
    (u'\u4fdd\u52a0\u5229\u4e9a Republic of Bulgaria', u'\u7d22\u975e\u4e9a Sofia'),
    (u'\u5e0c\u814a Hellenic Republic', u'\u96c5\u5178 Athens'),
    (u'\u57c3\u53ca Arab Republic of Egypt', u'\u5f00\u7f57 Cairo'),
    (u'\u82cf\u4e39 Republic of the Sudan', u'\u5580\u571f\u7a46 Khartoum'),
    (u'\u5357\u82cf\u4e39 The Republic of South Sudan', u'\u62c9\u59c6\u585e\u5c14 Ramciel'),
    (
        u'\u57c3\u585e\u4fc4\u6bd4\u4e9a Federal Democratic Republic of Ethiopia',
        u'\u4e9a\u7684\u65af\u4e9a\u8d1d\u5df4 Addis Ababa'
    ), (u'\u5384\u7acb\u7279\u91cc\u4e9a State of Eritrea', u'\u963f\u65af\u9a6c\u62c9 Asmara'),
    (u'\u5409\u5e03\u63d0 Republic of Djibouti', u'\u5409\u5e03\u63d0 Djibouti'),
    (u'\u7d22\u9a6c\u91cc Somali Republic', u'\u6469\u52a0\u8fea\u6c99 Mogadishu'),
    (u"\u5229\u6bd4\u4e9a Socialist People's Libyan Arab Jamahiriya", u'\u7684\u9ece\u6ce2\u91cc Tripoli'),
    (u"\u963f\u5c14\u53ca\u5229\u4e9a Democratic People's Republic of Algeria", u'\u963f\u5c14\u53ca\u5c14 Algiers'),
    (u'\u7a81\u5c3c\u65af Republic of Tunisia', u'\u7a81\u5c3c\u65af Tunis'),
    (u'\u6469\u6d1b\u54e5 Kingdom of Morocco', u'\u62c9\u5df4\u7279 Rabat'),
    (u'\u4f5b\u5f97\u89d2 Republic of Cape Verde', u'\u666e\u62c9\u4e9a Praia'),
    (u'\u6bdb\u91cc\u5854\u5c3c\u4e9a Islamic Republic of Mauritania', u'\u52aa\u74e6\u514b\u8096\u7279 Nouakchott'),
    (u'\u9a6c\u91cc Republic of Mali', u'\u5df4\u9a6c\u79d1 Bamako'),
    (u'\u585e\u5185\u52a0\u5c14 Republic of Senegal', u'\u8fbe\u5580\u5c14 Dakar'),
    (u'\u5188\u6bd4\u4e9a Republic of the Gambia', u'\u73ed\u73e0\u5c14 Banjul'),
    (u'\u51e0\u5185\u4e9a\u6bd4\u7ecd Republic of Guinea-Bissau', u'\u6bd4\u7ecd Bissau'),
    (u'\u51e0\u5185\u4e9a Republic of Guinea', u'\u79d1\u7eb3\u514b\u91cc Conakry'),
    (u'\u585e\u62c9\u5229\u6602 Republic of Sierra Leone', u'\u5f17\u91cc\u6566 Freetown'),
    (u'\u5229\u6bd4\u91cc\u4e9a Republic of Liberia', u'\u8499\u7f57\u7ef4\u4e9a Monrovia'),
    (u"\u79d1\u7279\u8fea\u74e6 Republic of Cote d'Ivoire", u'\u4e9a\u7a46\u82cf\u514b\u7f57 Yamoussoukro'),
    (u'\u5e03\u57fa\u7eb3\u6cd5\u7d22 Burkina Faso', u'\u74e6\u52a0\u675c\u53e4 Ouagadougou'),
    (u'\u5c3c\u65e5\u5c14 Republic of Niger', u'\u5c3c\u4e9a\u7f8e Niamey'),
    (u'\u4e4d\u5f97 Republic of Chad', u"\u6069\u8d3e\u6885\u7eb3 N'Djamena"),
    (u'\u5c3c\u65e5\u5229\u4e9a Federal Republic of Nigeria', u'\u963f\u5e03\u8d3e Abuja'),
    (u'\u52a0\u7eb3 Republic of Ghana', u'\u963f\u514b\u62c9 Accra'),
    (u'\u591a\u54e5 Togolese Republic', u'\u6d1b\u7f8e Lome'),
    (u'\u8d1d\u5b81 Republic of Benin', u'\u6ce2\u591a\u8bfa\u4f0f Porto-Novo'),
    (u'\u5580\u9ea6\u9686 Republic of Cameroon', u'\u96c5\u6e29\u5f97 Yaounde'),
    (u'\u52a0\u84ec Gabonese Republic', u'\u5229\u4f2f\u7ef4\u5c14 Libreville'),
    (u'\u8d64\u9053\u51e0\u5185\u4e9a Republic of Equatorial Guinea', u'\u9a6c\u62c9\u535a Malabo'),
    (
        u'\u5723\u591a\u7f8e\u548c\u666e\u6797\u897f\u6bd4 Democratic Republic of Sao Tome and Principe',
        u'\u5723\u591a\u7f8e Sao Tome'
    ), (u'\u4e2d\u975e Central African Republic', u'\u73ed\u5409 Bangui'),
    (u'\u521a\u679c Republic of the Congo', u'\u5e03\u62c9\u67f4\u7ef4\u5c14 Brazzaville'),
    (u'\u521a\u679c\u6c11\u4e3b\u5171\u548c\u56fd Democratic Republic of the Congo', u'\u91d1\u6c99\u8428 Kinshasa'),
    (u'\u4e4c\u5e72\u8fbe Republic of Uganda', u'\u574e\u5e15\u62c9 Kampala'),
    (u'\u5362\u65fa\u8fbe Republic of Rwanda', u'\u57fa\u52a0\u5229 Kigali'),
    (u'\u5e03\u9686\u8fea Republic of Burundi', u'\u5e03\u743c\u5e03\u62c9 Bujumbura'),
    (u'\u5766\u6851\u5c3c\u4e9a United Republic of Tanzania', u'\u591a\u591a\u9a6c Dodoma'),
    (u'\u80af\u5c3c\u4e9a Republic of Kenya', u'\u5185\u7f57\u534e Nairobi'),
    (u'\u5b89\u54e5\u62c9 Republic of Angola', u'\u7f57\u5b89\u8fbe Luanda'),
    (u'\u8d5e\u6bd4\u4e9a Republic of Zambia', u'\u5362\u8428\u5361 Lusaka'),
    (u'\u9a6c\u62c9\u7ef4 Republic of Malawi', u'\u5229\u9686\u572d Lilongwe'),
    (u'\u83ab\u6851\u6bd4\u514b Republic of Mozambique', u'\u9a6c\u666e\u6258 Maputo'),
    (u'\u9a6c\u8fbe\u52a0\u65af\u52a0 Republic of Madagascar', u'\u5854\u90a3\u90a3\u5229\u4f5b Antananarivo'),
    (u'\u79d1\u6469\u7f57 Union of the Comoros', u'\u83ab\u7f57\u5c3c Moroni'),
    (u'\u585e\u820c\u5c14 Republic of Seychelles', u'\u7ef4\u591a\u5229\u4e9a Victoria'),
    (u'\u6bdb\u91cc\u6c42\u65af Republic of Mauritius', u'\u8def\u6613\u6e2f Port Louis'),
    (u'\u6d25\u5df4\u5e03\u97e6 Republic of Zimbabwe', u'\u54c8\u62c9\u96f7 Harare'),
    (u'\u535a\u8328\u74e6\u7eb3 Republic of Botswana', u'\u54c8\u535a\u7f57\u5185 Gaborone'),
    (u'\u7eb3\u7c73\u6bd4\u4e9a Republic of Namibia', u'\u6e29\u5f97\u548c\u514b Windhoek'),
    (u'\u65af\u5a01\u58eb\u5170 Kingdom of Swaziland', u'\u59c6\u5df4\u5df4\u5185 Mbabane'),
    (u'\u83b1\u7d22\u6258 Kingdom of Lesotho', u'\u9a6c\u585e\u5362 Maseru'),
    (u'\u5357\u975e Republic of South Africa', u'\u6bd4\u52d2\u9640\u5229\u4e9a Cape Town'),
    (u'\u6fb3\u5927\u5229\u4e9a Commonwealth of Australia', u'\u582a\u57f9\u62c9 Canberra'),
    (
        u'\u5df4\u5e03\u4e9a\u65b0\u51e0\u5185\u4e9a Independent State of Papua New Guinea',
        u'\u83ab\u5c14\u5179\u6bd4\u6e2f Port Moresby'
    ), (u'\u6240\u7f57\u95e8\u7fa4\u5c9b Solomon Islands', u'\u970d\u5c3c\u4e9a\u62c9 Honiara'),
    (u'\u74e6\u52aa\u963f\u56fe Republic of Vanuatu', u'\u7ef4\u62c9\u6e2f Port-Vila'),
    (u'\u65b0\u897f\u5170 New Zealand', u'\u60e0\u7075\u987f Wellington'),
    (u'\u6590\u6d4e Republic of the Fiji Islands', u'\u82cf\u74e6 Suva'),
    (u'\u6c64\u52a0 Kingdom of Tonga', u"\u52aa\u5e93\u963f\u6d1b\u6cd5 Nuku'alofa"),
    (u'\u7459\u9c81 Republic of Nauru', u'\u4e9a\u4f26 Yaren District'),
    (u'\u57fa\u91cc\u5df4\u65af Republic of Kiribati', u'\u5854\u62c9\u74e6 Tarawa'),
    (u'\u56fe\u74e6\u5362 Tuvalu', u'\u5bcc\u7eb3\u5bcc\u63d0 Funafuti'),
    (u'\u8428\u6469\u4e9a Independent State of Samoa', u'\u963f\u76ae\u4e9a Apia'),
    (
        u'\u5bc6\u514b\u7f57\u5c3c\u897f\u4e9a\u8054\u90a6 Federated States of Micronesia',
        u'\u5e15\u5229\u57fa\u5c14 Palikir'
    ), (u'\u9a6c\u7ecd\u5c14\u7fa4\u5c9b Republic of the Marshall Islands', u'\u9a6c\u6731\u7f57 Majuro'),
    (u'\u5e15\u52b3 Republic of Palau', u'\u6885\u83b1\u51ef\u5965\u514b Melekeok'),
    (u'\u52a0\u62ff\u5927 Canada', u'\u6e25\u592a\u534e Ottawa'),
    (
        u'\u7f8e\u56fd United States of America',
        u'\u534e\u76db\u987f\u54e5\u4f26\u6bd4\u4e9a\u7279\u533a Washington D.C.'
    ), (u'\u58a8\u897f\u54e5 United Mexican States', u'\u58a8\u897f\u54e5\u57ce Mexico City'),
    (u'\u5371\u5730\u9a6c\u62c9 Republic of Guatemala', u'\u5371\u5730\u9a6c\u62c9 Guatemala'),
    (u'\u4f2f\u5229\u5179 Belize', u'\u8d1d\u5c14\u83ab\u6f58 Belmopan'),
    (u'\u8428\u5c14\u74e6\u591a Republic of El Salvador', u'\u5723\u8428\u5c14\u74e6\u591a San Salvador'),
    (u'\u6d2a\u90fd\u62c9\u65af Republic of Honduras', u'\u7279\u53e4\u897f\u52a0\u5c14\u5df4 Tegucigalpa'),
    (u'\u5c3c\u52a0\u62c9\u74dc Republic of Nicaragua', u'\u9a6c\u90a3\u74dc Managua'),
    (u'\u54e5\u65af\u8fbe\u9ece\u52a0 Republic of Costa Rica', u'\u5723\u4f55\u585e San Jose'),
    (u'\u5df4\u62ff\u9a6c Republic of Panama', u'\u5df4\u62ff\u9a6c\u57ce Panama'),
    (u'\u53e4\u5df4 Republic of Cuba', u'\u54c8\u74e6\u90a3 Havana'),
    (u'\u5df4\u54c8\u9a6c Commonwealth of the Bahamas', u'\u62ff\u9a9a Nassau'),
    (u'\u6d77\u5730 Republic of Haiti', u'\u592a\u5b50\u6e2f Port-au-Prince'),
    (u'\u591a\u7c73\u5c3c\u52a0\u5171\u548c\u56fd Dominican Republic', u'\u5723\u591a\u660e\u5404 Santo Domingo'),
    (u'\u7259\u4e70\u52a0 Jamaica', u'\u91d1\u65af\u6566 Kingston'),
    (
        u'\u5723\u57fa\u8328\u548c\u5c3c\u7ef4\u65af Federation of Saint Kitts and Nevis',
        u'\u5df4\u65af\u7279\u5c14 Basseterre'
    ), (u'\u5b89\u63d0\u74dc\u548c\u5df4\u5e03\u8fbe Antigua and Barbuda', u"\u5723\u7ea6\u7ff0 Saint John's"),
    (u'\u591a\u7c73\u5c3c\u514b Commonwealth of Dominica', u'\u7f57\u7d22 Roseau'),
    (u'\u5723\u5362\u897f\u4e9a Saint Lucia', u'\u5361\u65af\u7279\u91cc Castries'),
    (
        u'\u5723\u6587\u68ee\u7279\u548c\u683c\u6797\u7eb3\u4e01\u65af Saint Vincent and the Grenadines',
        u'\u91d1\u65af\u6566 Kingstown'
    ), (u'\u683c\u6797\u7eb3\u8fbe Grenada', u"\u5723\u4e54\u6cbb Saint George's"),
    (u'\u5df4\u5df4\u591a\u65af Barbados', u'\u5e03\u91cc\u5947\u6566 Bridgetown'),
    (
        u'\u7279\u7acb\u5c3c\u8fbe\u548c\u591a\u5df4\u54e5 Republic of Trinidad and Tobago',
        u'\u897f\u73ed\u7259\u6e2f Port of Spain'
    ), (u'\u8499\u7279\u585e\u62c9\u7279\u6216\u8499\u745f\u62c9\u7279\u5c9b Montserrat', u'\u5e03\u83b1\u5179 Brades'),
    (u'\u54e5\u4f26\u6bd4\u4e9a Republic of Colombia', u'\u6ce2\u54e5\u5927 Bogota'),
    (u'\u5384\u74dc\u591a\u5c14 Republic of Ecuador', u'\u57fa\u591a Quito'),
    (u'\u59d4\u5185\u745e\u62c9 Bolivarian Republic of Venezuela', u'\u52a0\u62c9\u52a0\u65af Caracas'),
    (u'\u572d\u4e9a\u90a3 Co-operative Republic of Guyana', u'\u4e54\u6cbb\u6566 Georgetown'),
    (u'\u82cf\u91cc\u5357 Republic of Suriname', u'\u5e15\u62c9\u9a6c\u91cc\u535a aramaribo'),
    (u'\u79d8\u9c81 Republic of Peru', u'\u5229\u9a6c Lima'),
    (u'\u73bb\u5229\u7ef4\u4e9a Republic of Bolivia', u'\u82cf\u514b\u96f7 Sucre'),
    (u'\u5df4\u62c9\u572d Republic of Paraguay', u'\u4e9a\u677e\u68ee Asuncion'),
    (u'\u963f\u6839\u5ef7 Argentine Republic', u'\u5e03\u5b9c\u8bfa\u65af\u827e\u5229\u65af Buenos Aires'),
    (u'\u4e4c\u62c9\u572d Oriental Republic of Uruguay', u'\u8499\u5f97\u7ef4\u7684\u4e9a Montevide'),
    (u'\u5df4\u897f Federative Republic of Brazil', u'\u5df4\u897f\u5229\u4e9a Brasilia'),
    (u'\u667a\u5229 Republic of Chile', u'\u5723\u5730\u4e9a\u54e5 Santiago')
]

sql = """INSERT INTO `question` (`content`, `options`, `answer`, `level`, `category`, `status`, `create_time`,
`update_time`) VALUES ("%s", "%s", %s, 2, 4, 1, "2022-09-21 21:13:57", "2022-09-21 21:13:57");"""


def capital():
    for x in CAPITALS:
        s = set()
        s.add(x[1])
        for y in range(0, 3):
            i = random.randint(0, 194)
            s.add(CAPITALS[i][1])
        s = list(s)
        random.shuffle(s)
        j = s.index(x[1]) + 1
        l = sql % ('{} 的首都是下列的那个城市？'.format(x[0]), '|'.join(s), j)
        print(l)



def country():
    for x in CAPITALS:
        s = set()
        s.add(x[0])
        for y in range(0, 3):
            i = random.randint(0, 194)
            s.add(CAPITALS[i][0])
        s = list(s)
        random.shuffle(s)
        j = s.index(x[0]) + 1
        l = sql % ('下列哪个国家的首都是{}？'.format(x[1]), '|'.join(s), j)
        print(l)


if __name__ == '__main__':
    country()
