##A simple example:
#### 1. Update own payment settings
http://localhost:9000/range-update --- with body
```json
{
    "lowestValue": 0,
    "highestValue": 100,
    "splitPoints": [
        50
    ],
    "rangeType": "PAYMENT"
}
```

#### 1. Update loan amount settings
http://localhost:9000/range-update --- with body
```json
{
    "lowestValue": 0,
    "highestValue": 2000000,
    "splitPoints": [
        1000000
    ],
    "rangeType": "AMOUNT"
}
```

#### 2. Check updated ranges
http://localhost:9000/range-get --- no params/ body required should return ranges

```json
{
  "ownPayment": [
    {
      "min": 0,
      "max": 50
    },
    {
      "min": 50,
      "max": 100
    }
  ],
  "loanAmount": [
    {
      "min": 0,
      "max": 1000000
    },
    {
      "min": 1000000,
      "max": 2000000
    }
  ]
}
```

#### 3. Put example values of each of ranges with margin values
http://localhost:9000/margin-add --- with body
```json
{
    "margins":[
        {
            "ownPaymentInPercentage": 70,
             "loanAmountPLN": 1500000,
              "margin": 2.60
        },
        {
            "ownPaymentInPercentage": 20,
             "loanAmountPLN": 500000,
              "margin": 3.50
        },
        {
            "ownPaymentInPercentage": 70,
             "loanAmountPLN": 500000,
              "margin": 3.30
        },
        {
            "ownPaymentInPercentage": 20,
             "loanAmountPLN": 1500000,
              "margin": 2.70
        }
    ]
}
```

#### 4. Get one margin 
http://localhost:9000/margin-get?currentLoanMonth=15&loanAmountPLN=50000&ownPaymentInPercentage=75

```json
{
    "margin": 3.3
}
```

#### 5. Get all data
http://localhost:9000/bank-margin-get --- no params/ body required should return ranges
```json
{
    "config": {
        "ownPayment": {
            "lowestValue": 0,
            "splitPoints": [
                50
            ],
            "highestValue": 100
        },
        "loanAmount": {
            "lowestValue": 0,
            "splitPoints": [
                1000000
            ],
            "highestValue": 2000000
        }
    },
    "margins": [
        {
            "ownPaymentRangeInPercent": {
                "min": 50,
                "max": 100
            },
            "loanAmountRangePLN": {
                "min": 1000000,
                "max": 2000000
            },
            "value": 2.6
        },
        {
            "ownPaymentRangeInPercent": {
                "min": 0,
                "max": 50
            },
            "loanAmountRangePLN": {
                "min": 0,
                "max": 1000000
            },
            "value": 3.5
        },
        {
            "ownPaymentRangeInPercent": {
                "min": 50,
                "max": 100
            },
            "loanAmountRangePLN": {
                "min": 0,
                "max": 1000000
            },
            "value": 3.3
        },
        {
            "ownPaymentRangeInPercent": {
                "min": 0,
                "max": 50
            },
            "loanAmountRangePLN": {
                "min": 1000000,
                "max": 2000000
            },
            "value": 2.7
        }
    ]
}
```


For swagger documentation go to 
http://localhost:9000/docs/swagger-ui

paste into the search form http://localhost:9000/docs/swagger.yml






