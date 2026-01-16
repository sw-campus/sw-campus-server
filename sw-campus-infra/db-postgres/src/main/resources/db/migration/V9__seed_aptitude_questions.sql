-- ========================================
-- 성향 테스트 (APTITUDE) 초기 문항 데이터
-- 15문항: Part1(4) + Part2(4) + Part3(7)
-- ========================================

-- 1. 문항 세트 생성
INSERT INTO swcampus.survey_question_sets (name, description, type, version, status, published_at)
VALUES (
    '개발자 성향 테스트 v1',
    '논리/사고력, 끈기/학습태도, 직무성향을 측정하는 15문항 테스트',
    'APTITUDE',
    1,
    'PUBLISHED',
    NOW()
);

-- 변수로 question_set_id 저장
DO $$
DECLARE
    v_set_id BIGINT;
    v_q1_id BIGINT;
    v_q2_id BIGINT;
    v_q3_id BIGINT;
    v_q4_id BIGINT;
    v_q5_id BIGINT;
    v_q6_id BIGINT;
    v_q7_id BIGINT;
    v_q8_id BIGINT;
    v_q9_id BIGINT;
    v_q10_id BIGINT;
    v_q11_id BIGINT;
    v_q12_id BIGINT;
    v_q13_id BIGINT;
    v_q14_id BIGINT;
    v_q15_id BIGINT;
BEGIN
    -- question_set_id 조회
    SELECT question_set_id INTO v_set_id
    FROM swcampus.survey_question_sets
    WHERE type = 'APTITUDE' AND status = 'PUBLISHED';

    -- ========================================
    -- Part 1: 논리 및 사고력 (4문항)
    -- 정답 맞추면 10점, 오답 0점
    -- ========================================

    -- Q1: 수열 문제
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 1, E'다음 수열에서 빈칸에 들어갈 숫자는?\n\n2, 6, 12, 20, 30, ?', 'RADIO', true, 'q1', 'PART1')
    RETURNING question_id INTO v_q1_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score, is_correct) VALUES
    (v_q1_id, 1, '40', '1', 0, false),
    (v_q1_id, 2, '42', '2', 10, true),
    (v_q1_id, 3, '44', '3', 0, false);

    -- Q2: 로봇 이동 문제
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 2, E'로봇 청소기가 다음 명령을 순서대로 실행합니다.\n\n1. 앞으로 2칸 이동\n2. 오른쪽으로 90도 회전\n3. 앞으로 1칸 이동\n4. 왼쪽으로 90도 회전\n5. 앞으로 2칸 이동\n\n로봇이 최종 도착한 위치와 바라보는 방향은?', 'RADIO', true, 'q2', 'PART1')
    RETURNING question_id INTO v_q2_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score, is_correct) VALUES
    (v_q2_id, 1, '원래 위치에서 오른쪽으로 1칸, 앞으로 4칸 / 위쪽', '1', 10, true),
    (v_q2_id, 2, '원래 위치에서 오른쪽으로 1칸, 앞으로 4칸 / 오른쪽', '2', 0, false),
    (v_q2_id, 3, '원래 위치에서 오른쪽으로 2칸, 앞으로 3칸 / 위쪽', '3', 0, false);

    -- Q3: 논리 문제
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 3, E'"모든 개발자는 커피를 좋아한다"가 참일 때, 반드시 참인 것은?', 'RADIO', true, 'q3', 'PART1')
    RETURNING question_id INTO v_q3_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score, is_correct) VALUES
    (v_q3_id, 1, '커피를 좋아하면 개발자이다', '1', 0, false),
    (v_q3_id, 2, '커피를 좋아하지 않으면 개발자가 아니다', '2', 10, true),
    (v_q3_id, 3, '개발자가 아니면 커피를 좋아하지 않는다', '3', 0, false);

    -- Q4: 범주화 문제
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 4, E'다음 중 나머지와 성격이 다른 하나는?\n\n사과, 바나나, 딸기, 과일, 포도', 'RADIO', true, 'q4', 'PART1')
    RETURNING question_id INTO v_q4_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score, is_correct) VALUES
    (v_q4_id, 1, '사과', '1', 0, false),
    (v_q4_id, 2, '딸기', '2', 0, false),
    (v_q4_id, 3, '과일', '3', 10, true);

    -- ========================================
    -- Part 2: 끈기 및 학습 태도 (4문항)
    -- 선택지별 점수: 1번=0점, 2번=5점, 3번=10점
    -- ========================================

    -- Q5
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 5, '코딩 중 에러가 발생했을 때, 당신의 첫 반응은?', 'RADIO', true, 'q5', 'PART2')
    RETURNING question_id INTO v_q5_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score) VALUES
    (v_q5_id, 1, '짜증이 나고 포기하고 싶다', '1', 0),
    (v_q5_id, 2, '일단 구글에 에러 메시지를 검색한다', '2', 5),
    (v_q5_id, 3, '원인을 분석하고 디버깅을 시작한다', '3', 10);

    -- Q6
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 6, '새로운 기계나 프로그램을 다룰 때 당신의 스타일은?', 'RADIO', true, 'q6', 'PART2')
    RETURNING question_id INTO v_q6_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score) VALUES
    (v_q6_id, 1, '설명서를 꼼꼼히 읽고 시작한다', '1', 5),
    (v_q6_id, 2, '일단 이것저것 눌러보며 익힌다', '2', 10),
    (v_q6_id, 3, '유튜브 튜토리얼을 보면서 따라한다', '3', 5);

    -- Q7
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 7, '단순 반복 작업을 할 때 당신의 생각은?', 'RADIO', true, 'q7', 'PART2')
    RETURNING question_id INTO v_q7_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score) VALUES
    (v_q7_id, 1, '그냥 빨리 끝내고 싶다', '1', 0),
    (v_q7_id, 2, '이걸 자동화할 수 있을까 고민한다', '2', 10),
    (v_q7_id, 3, '효율적인 패턴을 찾으려 한다', '3', 10);

    -- Q8
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 8, '개발자가 되고 싶은 가장 큰 이유는?', 'RADIO', true, 'q8', 'PART2')
    RETURNING question_id INTO v_q8_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, score) VALUES
    (v_q8_id, 1, '연봉이 높아서', '1', 0),
    (v_q8_id, 2, '내 아이디어를 직접 구현하고 싶어서', '2', 10),
    (v_q8_id, 3, '문제를 해결하는 과정이 재미있어서', '3', 10);

    -- ========================================
    -- Part 3: 직무 성향 (7문항)
    -- F=프론트엔드, B=백엔드, D=데이터
    -- ========================================

    -- Q9
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 9, '식당 창업을 한다면 가장 관심이 가는 분야는?', 'RADIO', true, 'q9', 'PART3')
    RETURNING question_id INTO v_q9_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q9_id, 1, '메뉴판 디자인과 인테리어', 'F', 'F'),
    (v_q9_id, 2, '주문 시스템과 재고 관리', 'B', 'B'),
    (v_q9_id, 3, '고객 데이터 분석과 마케팅', 'D', 'D');

    -- Q10
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 10, '레고 조립할 때 가장 희열을 느끼는 순간은?', 'RADIO', true, 'q10', 'PART3')
    RETURNING question_id INTO v_q10_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q10_id, 1, '완성된 결과물을 보는 순간', 'F', 'F'),
    (v_q10_id, 2, '기어가 맞물려 기계가 작동할 때', 'B', 'B'),
    (v_q10_id, 3, '설명서에서 패턴을 발견할 때', 'D', 'D');

    -- Q11
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 11, '엑셀이나 PPT 작업 시 당신의 스타일은?', 'RADIO', true, 'q11', 'PART3')
    RETURNING question_id INTO v_q11_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q11_id, 1, '시각적으로 깔끔하게 정리하는 것', 'F', 'F'),
    (v_q11_id, 2, '폴더 구조와 파일명을 체계적으로 관리', 'B', 'B'),
    (v_q11_id, 3, '함수와 수식으로 자동화하는 것', 'D', 'D');

    -- Q12
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 12, '앱 개발 팀 회의에서 가장 흥미로운 대화는?', 'RADIO', true, 'q12', 'PART3')
    RETURNING question_id INTO v_q12_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q12_id, 1, '"이 버튼 색상을 바꾸면 더 예쁠 것 같아요"', 'F', 'F'),
    (v_q12_id, 2, '"이 데이터는 어디서 가져오나요?"', 'B', 'B'),
    (v_q12_id, 3, '"사용자들이 이 기능을 얼마나 쓰나요?"', 'D', 'D');

    -- Q13
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 13, '앱 버그 중 가장 용서 못하는 것은?', 'RADIO', true, 'q13', 'PART3')
    RETURNING question_id INTO v_q13_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q13_id, 1, 'UI가 깨지거나 불편한 것', 'F', 'F'),
    (v_q13_id, 2, '로딩이 느리거나 서버가 죽는 것', 'B', 'B'),
    (v_q13_id, 3, '추천이 엉뚱하거나 검색이 이상한 것', 'D', 'D');

    -- Q14
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 14, '딱 하나만 만들 수 있다면?', 'RADIO', true, 'q14', 'PART3')
    RETURNING question_id INTO v_q14_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q14_id, 1, '화려하고 예쁜 웹사이트', 'F', 'F'),
    (v_q14_id, 2, '튼튼하고 빠른 서버', 'B', 'B'),
    (v_q14_id, 3, '똑똑한 AI 비서', 'D', 'D');

    -- Q15
    INSERT INTO swcampus.survey_questions (question_set_id, question_order, question_text, question_type, is_required, field_key, part)
    VALUES (v_set_id, 15, '서점에서 무심코 집어 든 책은?', 'RADIO', true, 'q15', 'PART3')
    RETURNING question_id INTO v_q15_id;

    INSERT INTO swcampus.survey_options (question_id, option_order, option_text, option_value, job_type) VALUES
    (v_q15_id, 1, '좋은 디자인의 비밀', 'F', 'F'),
    (v_q15_id, 2, '시스템을 설계하는 방법', 'B', 'B'),
    (v_q15_id, 3, '통계로 세상 읽기', 'D', 'D');

END $$;
