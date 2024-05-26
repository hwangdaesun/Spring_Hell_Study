안녕하십니까. 간만에 돌아온 커피요정 개발자입니다.

이번 과제는 여러분들이 알고 있는 구분형 권한 분리가 아닌 조건별 권한의 개념을 함께 느끼기 위해 제안된 과제입니다. ADMIN < USER < GUEST 이런 관계형 계층이 아닌 문서로 관리하는 세부 정책에 대해 명시하기 위한 과제입니다.

## IAM

권한 부여 및 관리 기능 구현 과제

아래 이미지는 IAM 서비스의 Data Diagram입니다.

(아래 Condition Block은 이번 과제에서 제시하지 않는 필드이니 무시하셔도 됩니다 )

<img width="621" alt="image" src="https://github.com/JNU-econovation/Spring_Hell_Study/assets/54030889/84ccf69f-f8bb-4ea7-99cf-109e4bb3b63a">


특정 서비스 및 리소스에 대한 접근 제어 및 권한 부여를 위한 서비스입니다.

## 구조

IAM Policy는 AWS 리소스에 대한 권한을 정의하는 JSON 문서입니다. 이 문서는 Optional top-level elements와 statement 두 가지 필드로 구성됩니다.

### **1. Optional top-level elements**

```java
{
  "Version": "2012-10-17"
}
```

### **1.1 Version**

```json

{
  "Statement": [
    {
      "Sid": "ExampleStatement1",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:ListBucket",
      "Resource": "arn:aws:s3:::example_bucket"
    },
    {
      "Sid": "ExampleStatement2",
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:DeleteObject",
      "Resource": "s3:::example_bucket/*"
    }
  ]
}
```

- **설명**: 정책 언어의 버전을 지정하는 필드입니다.
- **제한 사항**:
  - **`2012-10-17`**: 현재 정책 언어 버전입니다. 새로운 정책 작성 시 반드시 이 값을 사용해야 합니다. 이 버전을 사용하지 않으면 최신 기능을 사용할 수 없습니다.
  - **`2008-10-17`**: 이전 정책 언어 버전입니다. 새로운 정책 작성이나 기존 정책 업데이트 시 사용하지 마세요. 최신 기능이 작동하지 않습니다. 예를 들어, **`${aws:username}`** 같은 변수는 문자열로 인식됩니다.

### **1.2 ID**

- **설명**: 정책의 고유 식별자입니다. 자동으로 생성됩니다.
- **제한 사항**: 없음

### **2. Statement**

### **2.1 Statement List**

- **설명**: 하나 이상의 단일 문 또는 개별 문의 배열을 포함합니다.
- **제한 사항**: 리스트 형태로 여러 문을 포함할 수 있습니다.

### **2.2 Sid (Statement ID)**

- **설명**: 정책 내 개별 문에 대한 설명 또는 고유 식별자로 사용됩니다.
- **제한 사항**:
  - ASCII 대문자(A~~Z), 소문자(a~~z) 및 숫자(0~9)만 사용 가능합니다.
  - JSON 내에서 고유해야 합니다. 중복되면 안 됩니다.

### **2.3 Effect (필수 항목)**

- **설명**: 명시적인 허용(Allow) 또는 거부(Deny)를 지정하는 필드입니다.
- **제한 사항**:
  - **`Allow`** 또는 **`Deny`** 중 하나를 선택해야 합니다.
  - 기본적으로 모든 리소스는 거부됩니다. 명시적으로 허용하지 않으면 모두 거부됩니다.
  - 동일한 규제에 대해 Allow와 Deny가 동시에 존재하면 Deny가 우선 적용됩니다.

### **2.4 Principal**

- **설명**: 보안 주체(사용자, 그룹, 역할 등)를 지정합니다. 주로 리소스 기반 정책의 요소로 사용되는데,

    ```json
    "Principal" : { 
    "ECONO": [ 
      "123456789012",
      "555555555555" 
      ]
    }
    
    "Principal" : "*"
    위 2가지 형태 모두 표시가 가능합니다.
    
    ```

- **제한 사항**: 특정 형식의 문자열로 지정해야 합니다.
- Principal에 ECONO라고 적힌 경우 리스트로 해당하여 특정 계정의 Id의 list 형태를 받을 수 있습니다.

### **2.5 NotPrincipal**

- **설명**: 보안 주체를 제외하는 필드입니다.
- **제한 사항**: Principal과 동일하게 특정 형식의 문자열로 지정해야 합니다. 제한 사항은 Principal과 동일합니다.

### **2.6 Action**

- **설명**: 정책에서 허용하거나 거부하는 작업(액션)을 지정합니다.
- **제한 사항**: `:` 문자를 seperator로 지정합니다.  : 의 좌측은 정책을 허용할 서비스의 이름(s3, iam, sqs, sns, s3 … ) 를 명시할 수 있습니다.
- : 우측에 존재하는 문자는 GET, PUT, POST, DELETE 4가지만을 허용합니다.
- `MARK` 를 적용한 경우 들어오는 PII에 대해 비식별 처리를 진행합니다.
- **비식별 처리의 구현은 아래 3번 내용을 확인하세요**

### **2.7 NotAction**

- **설명**: 정책에서 제외할 작업(액션)을 지정합니다.
- **제한 사항**: Effect : Allow와 혼합해서 사용할 수 있습니다.

```json
"Effect": "Allow",
"NotAction": "DELETE",
"Resource": "s3:*",
```

위 예시를 살펴 보면 s3의 모든 리소스에 대해서 모든 행위를 허용하겠다라는 의미입니다. ( 단, DELETE 행위에 대해서는 위 정책을 포함하지 않겠다라는 의미입니다. )

### **2.8 Resource**

- **설명**: 정책에서 적용할 리소스를 지정합니다.
- **제한 사항**: *를 작성할 경우 모든 동작(GET, PUT, POST, DELETE)을 허용합니다.

```json
"Resource": "sqs"
```

위 예제는 sqs라는 서비스를 타겟으로 제한하는 것입니다.

String으로 자유롭게 명시는 가능합니다.

### **2.9 NotResource**

- **설명**: 정책에서 제외할 리소스를 지정합니다.
- **제한 사항**: Resource와 동일하게 특정 형식의 문자열로 지정해야 합니다.
  - 다른 제한사항은 Resource와 동일합니다.

### **리터럴 문자열 변환 기능**

- **설명**: 정책 변수의 리터럴 문자열 변환 기능을 on/off하는 기능입니다.
- **제한 사항**:
  - **`2012-10-17`** 버전에서는 정책 변수가 변수로 인식됩니다.
  - **`2008-10-17`** 버전에서는 정책 변수가 리터럴 문자열로 취급됩니다.

이 설명을 통해 IAM Policy의 구성 요소와 각 요소의 제한 사항을 이해할 수 있습니다. 정책을 작성할 때 이 지침을 따라야만 정책이 올바르게 작동하며, 최신 기능을 활용할 수 있습니다.

# 3. PII(개인 식별 정보) 비식별화

### 개인 식별 정보란?

개인 식별 정보(PII)는 **개인을 식별하는 데 사용할 수 있는 모든 데이터**입니다. 개인과 직간접적으로 연결되는 모든 정보는 PII로 간주됩니다. 이름, 이메일 주소, 전화번호, 은행 계좌 번호, 정부 발급 신분증 번호는 모두 PII의 예입니다.

이 때 MARK로 IAM Action이 명시된 경우 다음과 같은 비식별화를 진행하세요

### 제한사항

위 과제는 전화번호, 주민등록 번호를 대상으로만 진행하도록 하겠습니다.

전화번호 :  (010-1111-2222)의 경우 맨 뒷자리를 제외한 가운데 1111에 대해서 XXXX로 대체합니다. (010-XXXX-2222)

주민등록 번호 : 950000-1234567 일 경우 맨 뒤 번호 6자리를 X로 비식별화합니다. ( 950000-1XXXXXX)

위 2가지 대상에 대한 제한 및 변경 기능을 추가하세요.

위 개인 식별 정보는 모든 사람은(예외 인원은 없습니다) 비식별화된 정보만 열람이 가능하다.

ps) 위 제한사항은 개인정보 보호법 준수를 위한 비식별 조치 가이드라인에 착안하여 제안드리는 과제입니다.

아래 링크를 궁금하시다면 확인해보세요.
https://www.mois.go.kr/frt/bbs/type010/commonSelectBoardArticle.do?bbsId=BBSMSTR_000000000008&nttId=55287

# 요구사항

위 서비스의 제한사항을 구현하고 특정 요청에 대해 요청이 가능한지 true/false를 반환하는 API를 작성하세요.

Http Method : GET , POST, …

url : /check/resource/{resource}

data : {key value형의 Map data ( 자유도에 맏기도록 하겠습니다. )