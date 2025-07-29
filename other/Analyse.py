# Author: Mugaaaaa
# Date: 2025-07-25
# Description: 用于解析祖传军理刷题软件的题库并将题目数据导入SQLite数据库的脚本

from pathlib import Path
import re
import sqlite3

# 匹配题号、题干主体（不含答案）、答案本身
# 逻辑: "^"行首断言. 匹配"1""1.""1:"开头, "(A)""(AB)"等结尾的文本块
# 捕获组: 1.题号 2.题干 3.包括括号的答案
pattern_stem = re.compile(r"^(\d+)[:.](.*(\([A-E]+\)).*)", re.M)

# 匹配每个选项，从 A 到 E
# 逻辑: 选项开头可能是"A""A.", 用正向先行断言判断终止位置(块末尾或下一个选项开头)
# 补货组: 1.选项字母 2.选项文本
pattern_opts = re.compile(r"([A-E])(.*?)(?=[A-E]|\Z)", re.S)

file_path = Path('data1.bin')
db_name = 'data.db'
text = file_path.read_text()

# 分割每个题目块
# 逻辑: 分割的块的开头要是换行符, 用正向先行断言判断终止位置(下一题开头的题号)
text_blocks = re.split(r"\n(?=\d+[:\.])", text)

with sqlite3.connect(db_name) as conn:
    cursor = conn.cursor()
    try: 
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS questions (
                no INTEGER PRIMARY KEY,
                stem TEXT,
                answ TEXT,
                op_A TEXT,
                op_B TEXT,
                op_C TEXT,
                op_D TEXT,
                op_E TEXT,
                stat INTEGER DEFAULT 0
            )
        ''')

        for block in text_blocks:
            stem = pattern_stem.search(block).group()
            opts = pattern_opts.findall(block)[1:]
            answ = re.search(r'\([A-E]+\)', block).group()[1:-1]
            stem_without_answ = re.sub(r'\([A-E]+\)', '()', stem)
            if len(answ) > 1:
                stem_without_answ += "（多选题）"   # 如果是多选题就标注一下

            #print(stem_without_answ)
            #print(answ)
            #print(opts)

            opts_dict = {opt[0]: opt[1].strip().lstrip(':.') for opt in opts}
            to_insert = (
                stem_without_answ,
                answ,
                opts_dict.get('A'),
                opts_dict.get('B'),
                opts_dict.get('C'),
                opts_dict.get('D'),
                opts_dict.get('E')
            )

            cursor.execute('''
                INSERT INTO questions (
                    stem, answ, op_A, op_B, op_C, op_D, op_E
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?
                )
            ''', to_insert)

        conn.commit()
        
    except Exception as e:
        print(f"错误:{e}")
        conn.rollback()

    finally:
        cursor.close()
