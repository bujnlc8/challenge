# coding=utf-8

from __future__ import unicode_literals

import pymysql
from pymysql.constants import CLIENT

from datetime import datetime

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
    with open('question-xxqg.txt', 'r') as f:
        data = f.read().decode('utf8').split('~~~')
        index = 0
        sql = ''
        for x in data:
            x = x.strip().split('\n')
            content = x[0]
            options = '|'.join(x[1:-1])
            answer = 0
            answer_text = x[-1]
            if x[1].strip() in answer_text:
                answer = 1
            elif x[2].strip() in answer_text:
                answer = 2
            elif len(x) >= 5 and x[3].strip() in answer_text:
                answer = 3
            elif len(x) >= 6 and x[4].strip() in answer_text:
                answer = 4
            if answer == 0:
                print(content)
                continue
            sql += TPL % (content, options, answer, 1, 1, 1, datetime.now(), datetime.now())
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


if __name__ == '__main__':
    main()
