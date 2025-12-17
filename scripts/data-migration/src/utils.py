import pandas as pd
import os

def load_csv(path):
    """
    CSV 파일을 로드하여 DataFrame으로 반환합니다.
    인코딩은 utf-8을 우선 시도하고, 실패 시 cp949를 시도합니다.
    """
    if not os.path.exists(path):
        print(f"Warning: File not found: {path}")
        return None
        
    try:
        return pd.read_csv(path, encoding='utf-8-sig')
    except UnicodeDecodeError:
        try:
            return pd.read_csv(path, encoding='cp949')
        except Exception as e:
            print(f"Error reading {path}: {e}")
            return None

def clean_str(text):
    """
    문자열 앞뒤 공백을 제거하고, NaN이나 빈 값은 빈 문자열로 변환합니다.
    """
    if pd.isna(text) or text == '':
        return ''
    return str(text).strip()

def clean_int(text, default=0):
    """
    문자열에서 콤마를 제거하고 정수로 변환합니다.
    변환 실패 시 default 값을 반환합니다.
    """
    if pd.isna(text) or text == '':
        return default
    try:
        # 콤마 제거 및 공백 제거
        clean_text = str(text).replace(',', '').strip()
        # 소수점이 있는 경우 정수부만 취함
        if '.' in clean_text:
            clean_text = clean_text.split('.')[0]
        return int(clean_text)
    except ValueError:
        return default

def parse_date(text):
    """
    YYYYMMDD 형식의 문자열을 YYYY-MM-DD 형식으로 변환합니다.
    형식이 맞지 않으면 원본을 반환하거나 None을 반환할 수 있습니다.
    여기서는 형식이 맞지 않으면 None을 반환하도록 합니다.
    """
    if pd.isna(text) or text == '':
        return None
    
    s = str(text).strip()
    # 숫자만 남기기
    s = ''.join(filter(str.isdigit, s))
    
    if len(s) == 8:
        return f"{s[:4]}-{s[4:6]}-{s[6:]}"
    return None
