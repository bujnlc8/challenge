# coding=utf-8

from __future__ import unicode_literals
import os

import pymysql
from pymysql.constants import CLIENT

from datetime import datetime

import random

MYSQL_HOST = '127.0.0.1'
MYSQL_PORT = 33060
MYSQL_USER = 'root'
MYSQL_PASSWD = '123456'

TPL = '''INSERT INTO `question` (`content`, `options`, `answer`, `level`, `category`,
`status`, `create_time`, `update_time`) VALUES
('%s', '%s', %s, %s, %s, %s, '%s', '%s');'''


def get_connection():
    return pymysql.connect(
        host=MYSQL_HOST,
        port=MYSQL_PORT,
        user=MYSQL_USER,
        passwd=MYSQL_PASSWD,
        db='challenge',
        charset='utf8mb4',
        client_flag=CLIENT.MULTI_STATEMENTS,
    )


def main():
    con = get_connection()
    with open('shicidahui1.txt', 'r') as f:
        data = f.read().decode('utf8').split('~~~~')
        index = 0
        sql = ''
        for x in data:
            x = x.strip().split('\n')
            content = x[0]
            options = '|'.join(x[1:])
            print(x)
            if 'A' in content:
                answer = 1
                content = content.replace('A', '')
            elif 'B' in content:
                answer = 2
                content = content.replace('B', '')
            elif 'C' in content:
                answer = 3
                content = content.replace('C', '')
            elif 'D' in content:
                answer = 4
                content = content.replace('D', '')
            elif 'E' in content:
                answer = 5
                content = content.replace('E', '')
            sql += TPL % (
                pymysql.escape_string(content), pymysql.escape_string(
                    options.strip().replace(' ', '').replace('　', '')
                ), answer, 1, 8, 1, datetime.now(), datetime.now()
            )
            index += 1
            if index == 500:
                with con.cursor() as cursor:
                    cursor.execute(sql)
                    con.commit()
                    sql = ''
                    index = 0
        if sql:
            with con.cursor() as cursor:
                cursor.execute(sql)
                con.commit()
    con.close()


def kaixincidian():
    f = open('question-092207.txt')
    s = ['是', '能', '会', '可以', '对', '好']
    sql = ''
    for x in f:
        e = False
        x = x.strip().decode('utf-8')
        for y in s:
            if '（{}）'.format(y) in x:
                content = x[:x.index('（{}）'.format(y))]
                content, options, answer = content, '{}|不{}'.format(y, y), 1
                e = True
            if '（不{}）'.format(y) in x:
                content = x[:x.index('（不{}）'.format(y))]
                content, options, answer = content, '{}|不{}'.format(y, y), 2
                e = True
        if '（有）' in x:
            content = x[:x.index('（有）')]
            content, options, answer = content, '有|没有', 1
            e = True
        elif '（没有）' in x:
            content = x[:x.index('（没有）')]
            content, options, answer = content, '有|没有', 2
            e = True
        if not e:
            print('not valid', x)
        sql += TPL % (
            pymysql.escape_string(content), \
            pymysql.escape_string(options.strip().replace(' ', '').replace('　', '')), \
            answer, 1, 2, 1, datetime.now(), datetime.now(),
        )
    con = get_connection()
    with con.cursor() as cursor:
        cursor.execute(sql)
        con.commit()


def kuwandiqiu():
    # question,A,B,C,D,answer,difficulty(1-5),category,explanation,END_NO_DELETE
    f = open('EarthWiki_PureTextQuiz.txt')
    sql = ''
    for x in f:
        x = x.decode('utf8').strip().split(',')
        print(x[0])
        content, a, b, c, d, answer, difficulty, category, _, _ = x
        options = []
        if a:
            options.append(a.strip())
        if b:
            options.append(b.strip())
        if c:
            options.append(c.strip())
        if d:
            options.append(d.strip())
        answer_num = 0
        if answer == 'a':
            answer_num = 1
        elif answer == 'b':
            answer_num = 2
        elif answer == 'c':
            answer_num = 3
        elif answer == 'd':
            answer_num = 4
        diff = 1
        difficulty = int(difficulty)
        if difficulty <= 2:
            diff = 1
        elif difficulty <= 4:
            diff = 2
        else:
            diff = 3
        cc = 2
        if category == '历史':
            cc = 3
        elif category == '地理':
            cc = 4
        sql += TPL % (
            pymysql.escape_string(content), \
            pymysql.escape_string('|'.join(options).strip().replace(' ', '').replace('　', '')), \
            answer_num, diff, cc, 1, datetime.now(), datetime.now(),
        )
    con = get_connection()
    with con.cursor() as cursor:
        cursor.execute(sql)
        con.commit()


COUNTRY_CODE = [('ad', u'\u5b89\u9053\u5c14\u5171\u548c\u56fd', 'Andorra'),
                ('ae', u'\u963f\u62c9\u4f2f\u8054\u5408\u914b\u957f\u56fd', 'UnitedArabEmirates'),
                ('af', u'\u963f\u5bcc\u6c57', 'Afghanistan'),
                ('ag', u'\u5b89\u63d0\u74dc\u548c\u5df4\u5e03\u8fbe', 'AntiguaandBarbuda'),
                ('ai', u'\u5b89\u572d\u62c9\u5c9b', 'Anguilla'), ('al', u'\u963f\u5c14\u5df4\u5c3c\u4e9a', 'Albania'),
                ('am', u'\u4e9a\u7f8e\u5c3c\u4e9a', 'Armenia'), ('ao', u'\u5b89\u54e5\u62c9', 'Angola'),
                ('ar', u'\u963f\u6839\u5ef7', 'Argentina'), ('at', u'\u5965\u5730\u5229', 'Austria'),
                ('au', u'\u6fb3\u5927\u5229\u4e9a', 'Australia'), ('az', u'\u963f\u585e\u62dc\u7586', 'Azerbaijan'),
                ('bb', u'\u5df4\u5df4\u591a\u65af', 'Barbados'), ('bd', u'\u5b5f\u52a0\u62c9\u56fd', 'Bangladesh'),
                ('be', u'\u6bd4\u5229\u65f6', 'Belgium'), ('bf', u'\u5e03\u57fa\u7eb3\u6cd5\u7d22', 'Burkina-faso'),
                ('bg', u'\u4fdd\u52a0\u5229\u4e9a', 'Bulgaria'), ('bh', u'\u5df4\u6797', 'Bahrain'),
                ('bi', u'\u5e03\u9686\u8fea', 'Burundi'), ('bj', u'\u8d1d\u5b81', 'Benin'),
                ('bl', u'\u5df4\u52d2\u65af\u5766', 'Palestine'),
                ('bm', u'\u767e\u6155\u5927\u7fa4\u5c9b', 'BermudaIs.'), ('bn', u'\u6587\u83b1', 'Brunei'),
                ('bo', u'\u73bb\u5229\u7ef4\u4e9a', 'Bolivia'), ('br', u'\u5df4\u897f', 'Brazil'),
                ('bs', u'\u5df4\u54c8\u9a6c', 'Bahamas'), ('bw', u'\u535a\u8328\u74e6\u7eb3', 'Botswana'),
                ('by', u'\u767d\u4fc4\u7f57\u65af', 'Belarus'), ('bz', u'\u4f2f\u5229\u5179', 'Belize'),
                ('ca', u'\u52a0\u62ff\u5927', 'Canada'),
                ('cf', u'\u4e2d\u975e\u5171\u548c\u56fd', 'CentralAfricanRepublic'), ('cg', u'\u521a\u679c', 'Congo'),
                ('ch', u'\u745e\u58eb', 'Switzerland'), ('ck', u'\u5e93\u514b\u7fa4\u5c9b', 'CookIs.'),
                ('cl', u'\u667a\u5229', 'Chile'), ('cm', u'\u5580\u9ea6\u9686', 'Cameroon'),
                ('cn', u'\u4e2d\u56fd', 'China'), ('co', u'\u54e5\u4f26\u6bd4\u4e9a', 'Colombia'),
                ('cr', u'\u54e5\u65af\u8fbe\u9ece\u52a0', 'CostaRica'), ('cs', u'\u6377\u514b', 'Czech'),
                ('cu', u'\u53e4\u5df4', 'Cuba'), ('cy', u'\u585e\u6d66\u8def\u65af', 'Cyprus'),
                ('cz', u'\u6377\u514b', 'CzechRepublic'), ('de', u'\u5fb7\u56fd', 'Germany'),
                ('dj', u'\u5409\u5e03\u63d0', 'Djibouti'), ('dk', u'\u4e39\u9ea6', 'Denmark'),
                ('do', u'\u591a\u7c73\u5c3c\u52a0\u5171\u548c\u56fd', 'DominicaRep.'),
                ('dz', u'\u963f\u5c14\u53ca\u5229\u4e9a', 'Algeria'), ('ec', u'\u5384\u74dc\u591a\u5c14', 'Ecuador'),
                ('ee', u'\u7231\u6c99\u5c3c\u4e9a', 'Estonia'), ('eg', u'\u57c3\u53ca', 'Egypt'),
                ('es', u'\u897f\u73ed\u7259', 'Spain'), ('et', u'\u57c3\u585e\u4fc4\u6bd4\u4e9a', 'Ethiopia'),
                ('fi', u'\u82ac\u5170', 'Finland'), ('fj', u'\u6590\u6d4e', 'Fiji'), ('fr', u'\u6cd5\u56fd', 'France'),
                ('ga', u'\u52a0\u84ec', 'Gabon'), ('gb', u'\u82f1\u56fd', 'UnitedKiongdom'),
                ('gd', u'\u683c\u6797\u7eb3\u8fbe', 'Grenada'), ('ge', u'\u683c\u9c81\u5409\u4e9a', 'Georgia'),
                ('gf', u'\u6cd5\u5c5e\u572d\u4e9a\u90a3', 'FrenchGuiana'), ('gh', u'\u52a0\u7eb3', 'Ghana'),
                ('gi', u'\u76f4\u5e03\u7f57\u9640', 'Gibraltar'), ('gm', u'\u5188\u6bd4\u4e9a', 'Gambia'),
                ('gn', u'\u51e0\u5185\u4e9a', 'Guinea'), ('gr', u'\u5e0c\u814a', 'Greece'),
                ('gt', u'\u5371\u5730\u9a6c\u62c9', 'Guatemala'), ('gu', u'\u5173\u5c9b', 'Guam'),
                ('gy', u'\u572d\u4e9a\u90a3', 'Guyana'),
                ('hk', u'\u9999\u6e2f\u7279\u522b\u884c\u653f\u533a', 'Hongkong'),
                ('hn', u'\u6d2a\u90fd\u62c9\u65af', 'Honduras'), ('ht', u'\u6d77\u5730', 'Haiti'),
                ('hu', u'\u5308\u7259\u5229', 'Hungary'), ('id', u'\u5370\u5ea6\u5c3c\u897f\u4e9a', 'Indonesia'),
                ('ie', u'\u7231\u5c14\u5170', 'Ireland'), ('il', u'\u4ee5\u8272\u5217', 'Israel'),
                ('in', u'\u5370\u5ea6', 'India'), ('iq', u'\u4f0a\u62c9\u514b', 'Iraq'),
                ('ir', u'\u4f0a\u6717', 'Iran'), ('is', u'\u51b0\u5c9b', 'Iceland'),
                ('it', u'\u610f\u5927\u5229', 'Italy'), ('jm', u'\u7259\u4e70\u52a0', 'Jamaica'),
                ('jo', u'\u7ea6\u65e6', 'Jordan'), ('jp', u'\u65e5\u672c', 'Japan'),
                ('ke', u'\u80af\u5c3c\u4e9a', 'Kenya'), ('kg', u'\u5409\u5c14\u5409\u65af\u5766', 'Kyrgyzstan'),
                ('kh', u'\u67ec\u57d4\u5be8', 'Kampuchea(Cambodia)'), ('kp', u'\u671d\u9c9c', 'NorthKorea'),
                ('kr', u'\u97e9\u56fd', 'Korea'),
                ('kt', u'\u79d1\u7279\u8fea\u74e6\u5171\u548c\u56fd', 'RepublicofIvoryCoast'),
                ('kw', u'\u79d1\u5a01\u7279', 'Kuwait'), ('kz', u'\u54c8\u8428\u514b\u65af\u5766', 'Kazakstan'),
                ('la', u'\u8001\u631d', 'Laos'), ('lb', u'\u9ece\u5df4\u5ae9', 'Lebanon'),
                ('lc', u'\u5723\u5362\u897f\u4e9a', 'St.Lucia'),
                ('li', u'\u5217\u652f\u6566\u58eb\u767b', 'Liechtenstein'),
                ('lk', u'\u65af\u91cc\u5170\u5361', 'SriLanka'), ('lr', u'\u5229\u6bd4\u91cc\u4e9a', 'Liberia'),
                ('ls', u'\u83b1\u7d22\u6258', 'Lesotho'), ('lt', u'\u7acb\u9676\u5b9b', 'Lithuania'),
                ('lu', u'\u5362\u68ee\u5821', 'Luxembourg'), ('lv', u'\u62c9\u8131\u7ef4\u4e9a', 'Latvia'),
                ('ly', u'\u5229\u6bd4\u4e9a', 'Libya'), ('ma', u'\u6469\u6d1b\u54e5', 'Morocco'),
                ('mc', u'\u6469\u7eb3\u54e5', 'Monaco'), ('md', u'\u6469\u5c14\u591a\u74e6', 'Moldova,Republicof'),
                ('mg', u'\u9a6c\u8fbe\u52a0\u65af\u52a0', 'Madagascar'), ('ml', u'\u9a6c\u91cc', 'Mali'),
                ('mm', u'\u7f05\u7538', 'Burma'), ('mn', u'\u8499\u53e4', 'Mongolia'), ('mo', u'\u6fb3\u95e8', 'Macao'),
                ('ms', u'\u8499\u7279\u585e\u62c9\u7279\u5c9b', 'MontserratIs'), ('mt', u'\u9a6c\u8033\u4ed6', 'Malta'),
                ('mu', u'\u6bdb\u91cc\u6c42\u65af', 'Mauritius'), ('mv', u'\u9a6c\u5c14\u4ee3\u592b', 'Maldives'),
                ('mw', u'\u9a6c\u62c9\u7ef4', 'Malawi'), ('mx', u'\u58a8\u897f\u54e5', 'Mexico'),
                ('my', u'\u9a6c\u6765\u897f\u4e9a', 'Malaysia'), ('mz', u'\u83ab\u6851\u6bd4\u514b', 'Mozambique'),
                ('na', u'\u7eb3\u7c73\u6bd4\u4e9a', 'Namibia'), ('ne', u'\u5c3c\u65e5\u5c14', 'Niger'),
                ('ng', u'\u5c3c\u65e5\u5229\u4e9a', 'Nigeria'), ('ni', u'\u5c3c\u52a0\u62c9\u74dc', 'Nicaragua'),
                ('nl', u'\u8377\u5170', 'Netherlands'), ('no', u'\u632a\u5a01', 'Norway'),
                ('np', u'\u5c3c\u6cca\u5c14', 'Nepal'), ('nr', u'\u7459\u9c81', 'Nauru'),
                ('nz', u'\u65b0\u897f\u5170', 'NewZealand'), ('om', u'\u963f\u66fc', 'Oman'),
                ('pa', u'\u5df4\u62ff\u9a6c', 'Panama'), ('pe', u'\u79d8\u9c81', 'Peru'),
                ('pf', u'\u6cd5\u5c5e\u73bb\u5229\u5c3c\u897f\u4e9a', 'FrenchPolynesia'),
                ('pg', u'\u5df4\u5e03\u4e9a\u65b0\u51e0\u5185\u4e9a', 'PapuaNewCuinea'),
                ('ph', u'\u83f2\u5f8b\u5bbe', 'Philippines'), ('pk', u'\u5df4\u57fa\u65af\u5766', 'Pakistan'),
                ('pl', u'\u6ce2\u5170', 'Poland'), ('pr', u'\u6ce2\u591a\u9ece\u5404', 'PuertoRico'),
                ('pt', u'\u8461\u8404\u7259', 'Portugal'), ('py', u'\u5df4\u62c9\u572d', 'Paraguay'),
                ('qa', u'\u5361\u5854\u5c14', 'Qatar'), ('ro', u'\u7f57\u9a6c\u5c3c\u4e9a', 'Romania'),
                ('ru', u'\u4fc4\u7f57\u65af', 'Russia'), ('sa', u'\u6c99\u7279\u963f\u62c9\u4f2f', 'SaudiArabia'),
                ('sb', u'\u6240\u7f57\u95e8\u7fa4\u5c9b', 'SolomonIs'), ('sc', u'\u585e\u820c\u5c14', 'Seychelles'),
                ('sd', u'\u82cf\u4e39', 'Sudan'), ('se', u'\u745e\u5178', 'Sweden'),
                ('sg', u'\u65b0\u52a0\u5761', 'Singapore'), ('si', u'\u65af\u6d1b\u6587\u5c3c\u4e9a', 'Slovenia'),
                ('sk', u'\u65af\u6d1b\u4f10\u514b', 'Slovakia'), ('sl', u'\u585e\u62c9\u5229\u6602', 'SierraLeone'),
                ('sm', u'\u5723\u9a6c\u529b\u8bfa', 'SanMarino'), ('sn', u'\u585e\u5185\u52a0\u5c14', 'Senegal'),
                ('so', u'\u7d22\u9a6c\u91cc', 'Somali'), ('sr', u'\u82cf\u91cc\u5357', 'Suriname'),
                ('st', u'\u5723\u591a\u7f8e\u548c\u666e\u6797\u897f\u6bd4', 'SaoTomeandPrincipe'),
                ('sv', u'\u8428\u5c14\u74e6\u591a', 'EISalvador'), ('sy', u'\u53d9\u5229\u4e9a', 'Syria'),
                ('sz', u'\u65af\u5a01\u58eb\u5170', 'Swaziland'), ('td', u'\u4e4d\u5f97', 'Chad'),
                ('tg', u'\u591a\u54e5', 'Togo'), ('th', u'\u6cf0\u56fd', 'Thailand'),
                ('tj', u'\u5854\u5409\u514b\u65af\u5766', 'Tajikstan'),
                ('tm', u'\u571f\u5e93\u66fc\u65af\u5766', 'Turkmenistan'), ('tn', u'\u7a81\u5c3c\u65af', 'Tunisia'),
                ('to', u'\u6c64\u52a0', 'Tonga'), ('tr', u'\u571f\u8033\u5176', 'Turkey'),
                ('tt', u'\u7279\u7acb\u5c3c\u8fbe\u548c\u591a\u5df4\u54e5', 'TrinidadandTobago'),
                ('tz', u'\u5766\u6851\u5c3c\u4e9a', 'Tanzania'), ('ua', u'\u4e4c\u514b\u5170', 'Ukraine'),
                ('ug', u'\u4e4c\u5e72\u8fbe', 'Uganda'), ('us', u'\u7f8e\u56fd', 'UnitedStatesofAmerica'),
                ('uy', u'\u4e4c\u62c9\u572d', 'Uruguay'), ('uz', u'\u4e4c\u5179\u522b\u514b\u65af\u5766', 'Uzbekistan'),
                ('vc', u'\u5723\u6587\u68ee\u7279\u5c9b', 'SaintVincent'),
                ('ve', u'\u59d4\u5185\u745e\u62c9', 'Venezuela'), ('vn', u'\u8d8a\u5357', 'Vietnam'),
                ('ye', u'\u4e5f\u95e8', 'Yemen'), ('yu', u'\u5357\u65af\u62c9\u592b', 'Yugoslavia'),
                ('za', u'\u5357\u975e', 'SouthAfrica'), ('zm', u'\u8d5e\u6bd4\u4e9a', 'Zambia'),
                ('zr', u'\u624e\u4f0a\u5c14', 'Zaire'), ('zw', u'\u6d25\u5df4\u5e03\u97e6', 'Zimbabwe')]

CountryMap = {x[0]: x
              for x in COUNTRY_CODE}


def flag():
    flags = os.listdir('flag')
    common = []
    sql = ''
    for x in flags:
        if x.split('.')[0] in CountryMap:
            common.append(CountryMap[x.split('.')[0]])
    print(common, len(common))
    url = 'https://bucket-1256650966.cos.ap-beijing.myqcloud.com/flag/{}.png'
    for x in common:
        content = '下图是哪个国家的国旗？###' + url.format(x[0])
        c = set()
        c.add(x[1])
        for y in range(3):
            c.add(common[random.randint(0, 169)][1])
        c = list(c)
        random.shuffle(c)
        j = c.index(x[1]) + 1
        options = c
        # for x in c:
        #     options.append(url.format(x))
        sql += TPL % (
            pymysql.escape_string(content), \
            pymysql.escape_string('|'.join(options).strip().replace(' ', '').replace('　', '')), \
            j, 1, 4, 1, datetime.now(), datetime.now(),
        )
    con = get_connection()
    with con.cursor() as cursor:
        cursor.execute(sql)
        con.commit()


def maps():
    flags = os.listdir('maps')
    common = []
    sql = ''
    for x in flags:
        if x.split('.')[0] in CountryMap:
            common.append(CountryMap[x.split('.')[0]])
    print(common, len(common))
    url = 'https://bucket-1256650966.cos.ap-beijing.myqcloud.com/map/{}.png'
    for x in common:
        content = '下图是哪个国家的地图？###' + url.format(x[2])
        c = set()
        c.add(x[1])
        for y in range(3):
            c.add(common[random.randint(0, 134)][1])
        c = list(c)
        random.shuffle(c)
        j = c.index(x[1]) + 1
        options = c
        # for x in c:
        #     options.append(url.format(x))
        sql += TPL % (
            pymysql.escape_string(content), \
            pymysql.escape_string('|'.join(options).strip().replace(' ', '').replace('　', '')), \
            j, 1, 4, 1, datetime.now(), datetime.now(),
        )
    con = get_connection()
    with con.cursor() as cursor:
        cursor.execute(sql)
        con.commit()

if __name__ == '__main__':
    # kaixincidian()
    # kuwandiqiu()
    # flag()
    # maps()
    main()
