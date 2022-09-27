# coding=utf-8

from __future__ import unicode_literals

from bs4 import BeautifulSoup

import requests

URL = 'https://www.tiku.cn/index/index/questions.html?cid=17&cno=1&page={}&typeid=600072'

file = open('q.txt', 'w')


def scratch(page):
    url = URL.format(page)
    response = requests.get(url)
    response = response.text
    soup = BeautifulSoup(response, 'html.parser')
    a = soup.find(class_='col-md-8')
    b = a.find_all(class_='card mb-3 q-detail rounded-0')
    for c in b:
        d = c.find('span', class_='nav-link text-dark pl-0')
        answer = d.text
        e = c.find(class_='card-text mb-3')
        content = e.text
        f = [x.text for x in c.find_all(class_='col-xl-6')]
        file.write(content.strip().encode('utf-8'))
        file.write('\n')
        for x in f:
            file.write(x[2:].strip().encode('utf-8'))
            file.write('\n')
        file.write(answer.strip().encode('utf-8'))
        file.write('\n~~~~~\n')
        print(content.strip())


if __name__ == '__main__':
    for x in range(1, 421):
        try:
            scratch(x)
        except Exception as e:
            print(e)
