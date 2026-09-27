# ISP TESTS

---

### All Combinations Coverage (ACoC)
**Method**: `com.petify.petify.service.FavoriteService.getFavoritedListings`

Partitions:
* C1: `userId` is `null`
  * C1.1: `false`
  * C1.2: `true`
* C2: `userId` is:
  * C2.1: less than 0
  * C2.2: 0
  * C2.3: greater than 0

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.1 | C2.2 |   Yes    | 
| 3 | C1.1 | C2.3 |   Yes    |
| 4 | C1.2 | C2.1 |    No    |
| 5 | C1.2 | C2.2 |    No    |
| 6 | C1.2 | C2.3 |    No    |

method: `com.petify.petify.service.AuthService.getUserById`
* C1: `userId` is `null`:
  * C1.1: `false`
  * C1.2: `true`
* C2: user exists with `userId`
  * C2.1: `false`
  * C2.2 `true`

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.2 | C2.1 |   Yes    |
| 3 | C1.1 | C2.2 |   Yes    |
| 4 | C1.2 | C2.2 |    No    |

method: `com.petify.petify.service.AuthService.getUserByUsername`
* C1: `username` is `null`:
  * C1.1: `false`
  * C1.2: `true`
* C2: user exists with `username`:
  * C2.1: `false`
  * C2.2: `true`

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.2 | C2.1 |   Yes    |
| 3 | C1.1 | C2.2 |   Yes    |
| 4 | C1.2 | C2.2 |    No    |

method: `com.petify.petify.service.AuthService.isUserInTopActive`
* C1: `userId` is `null`:
  * C1.1: `false`
  * C1.2: `true`
* C2: outcome of `analyticsRepository.getTopActiveUsers`:
  * C2.1: raises an exception
  * C2.2: returns an empty list
  * C2.3: returns a non-empty list not containing `userId`
  * C2.4: returns a non-empty list containing `userId`

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.1 | C2.2 |   Yes    |
| 3 | C1.1 | C2.3 |   Yes    |
| 4 | C1.1 | C2.4 |   Yes    |
| 5 | C1.2 | C2.1 |   Yes    |
| 6 | C1.2 | C2.2 |   Yes    |
| 7 | C1.2 | C2.3 |   Yes    |
| 8 | C1.2 | C2.4 |    No    |

### Base Choice Coverage (BCC)
method: `com.petify.petify.service.FavoriteService.addFavorite`

Partitions:
* C1: `userId` is `null`
  * C1.1: `false`
  * C1.2: `true`
* C2: `userId` is:
  * C2.1: less than 0
  * C2.2: 0
  * C2.3: greater than 0
* C3: `listingId` is `null`
    * C3.1: `false`
    * C3.2: `true`
* C4: `listingId` is:
    * C4.1: less than 0
    * C4.2: 0
    * C4.3: greater than 0

| # |  C1  |  C2  | C3   | C4   | Feasible |
|:-:|:----:|:----:|------|------|:--------:|
| 1 | C1.1 | C2.3 | C3.1 | C4.3 |   Yes    |
| 2 | C1.2 | C2.3 | C3.1 | C4.3 |    No    |
| 3 | C1.1 | C2.2 | C3.1 | C4.3 |   Yes    |
| 4 | C1.1 | C2.1 | C3.1 | C4.3 |   Yes    |
| 5 | C1.1 | C2.3 | C3.2 | C4.3 |    No    |
| 6 | C1.1 | C2.3 | C3.1 | C4.2 |   Yes    |
| 7 | C1.1 | C2.3 | C3.1 | C4.1 |   Yes    |

### All Combinations Coverage (ACoC)
**Method**: `com.petify.petify.service.RecommendationService.getRecommendedListings(Long userId)`

Partitions:
* C1: `userId` is `null`
  * C1.1: `false`
  * C1.2: `true`
* C2: `userId` is:
  * C2.1: less than 0
  * C2.2: 0
  * C2.3: greater than 0

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.1 | C2.2 |   Yes    |
| 3 | C1.1 | C2.3 |   Yes    |
| 4 | C1.2 | C2.1 |    No    |
| 5 | C1.2 | C2.2 |    No    |
| 6 | C1.2 | C2.3 |    No    |

**Test set:** 1,2,3

### All Combinations Coverage (ACoC)
**Method**: `com.petify.petify.service.AnalyticsService.getTopActiveUsers(LocalDateTime startTs, LocalDateTime endTs)`

Partitions:
* C1: `startTs` is `null`
  * C1.1: `false`
  * C1.2: `true`
* C2: `endTs` is `null`
  * C2.1: `false`
  * C2.2: `true`

| # |  C1  |  C2  | Feasible |
|:-:|:----:|:----:|:--------:|
| 1 | C1.1 | C2.1 |   Yes    |
| 2 | C1.1 | C2.2 |   Yes    |
| 3 | C1.2 | C2.1 |   Yes    |
| 4 | C1.2 | C2.2 |   Yes    |

**Test set:** 1,2,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ReviewService.getReviewsByClinic(Long clinicId)`

* C1: `clinicId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `clinicId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 |   Yes    |
|    2     | C1.2 | C2.3 |    No    |
|    3     | C1.1 | C2.1 |   Yes    |
|    4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ReviewService.getMyClinicReview(Long reviewerId, Long clinicId)`

* C1: `reviewerId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `reviewerId` value (C2.1 negative / C2.2 zero / C2.3 positive)
* C3: `clinicId` is `null` (C3.1 `false` / C3.2 `true`)
* C4: `clinicId` value (C4.1 negative / C4.2 zero / C4.3 positive)

| #        |  C1  |  C2  |  C3  |  C4  | Feasible |
|:--------:|:----:|:----:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 | C3.1 | C4.3 |   Yes    |
|    2     | C1.2 | C2.3 | C3.1 | C4.3 |    No    |
|    3     | C1.1 | C2.1 | C3.1 | C4.3 |   Yes    |
|    4     | C1.1 | C2.2 | C3.1 | C4.3 |   Yes    |
|    5     | C1.1 | C2.3 | C3.2 | C4.3 |    No    |
|    6     | C1.1 | C2.3 | C3.1 | C4.1 |   Yes    |
|    7     | C1.1 | C2.3 | C3.1 | C4.2 |   Yes    |

**Test set:** 1,3,4,6,7

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ReviewService.getReviewsLeftByUser(Long reviewerId)`

* C1: `reviewerId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `reviewerId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|     #     |  C1  |  C2  | Feasible |
|:---------:|:----:|:----:|:--------:|
| 1 (base)  | C1.1 | C2.3 |   Yes    |
|     2     | C1.2 | C2.3 |    No    |
|     3     | C1.1 | C2.1 |   Yes    |
|     4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ListingService.createListing(Long userId, CreateListingRequest request)`

* C1: `userId` is `null`
  * C1.1: `false`
  * C1.2: `true`
* C2: `userId` value
  * C2.1: negative
  * C2.2: zero
  * C2.3: positive
* C3: `request.getAnimalId()` is `null`
  * C3.1: `false`
  * C3.2: `true`
* C4: `request.getAnimalId()` value
  * C4.1: negative
  * C4.2: zero
  * C4.3: positive
* C5: `request.getDescription()` is `null`
  * C5.1: `false`
  * C5.2: `true`
* C6: `request.getDescription()` is blank
  * C6.1: `false`
  * C6.2: `true`
* C7: `request.getPrice()` is `null`
  * C7.1: `false`
  * C7.2: `true`
* C8: `request.getPrice()` value
  * C8.1: negative
  * C8.2: zero
  * C8.3: positive



|    #     |  C1  |  C2  |  C3  |  C4  |  C5  |  C6  |  C7  |  C8  | Feasible |
|:--------:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.3 |   Yes    |
|    2     | C1.2 | C2.3 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.3 |    No    |
|    3     | C1.1 | C2.1 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.3 |   Yes    |
|    4     | C1.1 | C2.2 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.3 |   Yes    |
|    5     | C1.1 | C2.3 | C3.2 | C4.3 | C5.1 | C6.1 | C7.1 | C8.3 |    No    |
|    6     | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.3 |   Yes    |
|    7     | C1.1 | C2.3 | C3.1 | C4.2 | C5.1 | C6.1 | C7.1 | C8.3 |   Yes    |
|    8     | C1.1 | C2.3 | C3.1 | C4.3 | C5.2 | C6.1 | C7.1 | C8.3 |    No    |
|    9     | C1.1 | C2.3 | C3.1 | C4.3 | C5.1 | C6.2 | C7.1 | C8.3 |   Yes    |
|    10    | C1.1 | C2.3 | C3.1 | C4.3 | C5.1 | C6.1 | C7.2 | C8.3 |    No    |
|    11    | C1.1 | C2.3 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.1 |   Yes    |
|    12    | C1.1 | C2.3 | C3.1 | C4.3 | C5.1 | C6.1 | C7.1 | C8.2 |   Yes    |

**Test set:** 1,3,4,6,7,9,11,12

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ListingService.getListingById(Long listingId)`

* C1: `listingId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `listingId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 |   Yes    |
|    2     | C1.2 | C2.3 |    No    |
|    3     | C1.1 | C2.1 |   Yes    |
|    4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ListingService.getListingsByStatus(String status, Pageable pageable)`

* C1: `status` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `status` is blank (C2.1 `false` / C2.2 `true`)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.1 |   Yes    |
|    2     | C1.2 | C2.1 |    No    |
|    3     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.ListingService.getRecommendedListings(Long userId)`

* C1: `userId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `userId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 |   Yes    |
|    2     | C1.2 | C2.3 |    No    |
|    3     | C1.1 | C2.1 |   Yes    |
|    4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.AppointmentService.resolveClinicIdForUser(Long userId)`

* C1: `userId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `userId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 |   Yes    |
|    2     | C1.2 | C2.3 |    No    |
|    3     | C1.1 | C2.1 |   Yes    |
|    4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.AppointmentService.deleteUnavailableSlot(Long clinicId, Long slotId)`

* C1: `clinicId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `clinicId` value (C2.1 negative / C2.2 zero / C2.3 positive)
* C3: `slotId` is `null` (C3.1 `false` / C3.2 `true`)
* C4: `slotId` value (C4.1 negative / C4.2 zero / C4.3 positive)

|    #     |  C1  |  C2  |  C3  |  C4  | Feasible |
|:--------:|:----:|:----:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 | C3.1 | C4.3 |   Yes    |
|    2     | C1.2 | C2.3 | C3.1 | C4.3 |    No    |
|    3     | C1.1 | C2.1 | C3.1 | C4.3 |   Yes    |
|    4     | C1.1 | C2.2 | C3.1 | C4.3 |   Yes    |
|    5     | C1.1 | C2.3 | C3.2 | C4.3 |    No    |
|    6     | C1.1 | C2.3 | C3.1 | C4.1 |   Yes    |
|    7     | C1.1 | C2.3 | C3.1 | C4.2 |   Yes    |

**Test set:** 1,3,4,6,7

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.PetService.addPet(Long userId, CreatePetRequest request)`

* C1: `userId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `userId` value (C2.1 negative / C2.2 zero / C2.3 positive)
* C3: `request.getName()` is `null` (C3.1 `false` / C3.2 `true`)
* C4: `request.getName()` is blank (C4.1 `false` / C4.2 `true`)
* C5: `request.getSex()` is `null` (C5.1 `false` / C5.2 `true`)
* C6: `request.getSex()` is blank (C6.1 `false` / C6.2 `true`)
* C7: `request.getType()` is `null` (C7.1 `false` / C7.2 `true`)
* C8: `request.getType()` is blank (C8.1 `false` / C8.2 `true`)
* C9: `request.getSpecies()` is `null` (C9.1 `false` / C9.2 `true`)
* C10: `request.getSpecies()` is blank (C10.1 `false` / C10.2 `true`)
* C11: `request.getDateOfBirth()` is `null` — unvalidated (C11.1 `false` / C11.2 `true`)
* C12: `request.getBreed()` is `null` — unvalidated (C12.1 `false` / C12.2 `true`)
* C13: `request.getBreed()` is blank — unvalidated (C13.1 `false` / C13.2 `true`)
* C14: `request.getLocatedName()` is `null` — unvalidated (C14.1 `false` / C14.2 `true`)
* C15: `request.getLocatedName()` is blank — unvalidated (C15.1 `false` / C15.2 `true`)


|    #     |  C1  |  C2  |  C3  |  C4  |  C5  |  C6  |  C7  |  C8  |  C9  |  C10  |  C11  |  C12  |  C13  |  C14  |  C15  | Feasible |
|:--------:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:----:|:-----:|:-----:|:-----:|:-----:|:-----:|:-----:|:--------:|
| 1 (base) | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    2     | C1.2 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |    No    |
|    3     | C1.1 | C2.1 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    4     | C1.1 | C2.2 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    5     | C1.1 | C2.3 | C3.2 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |    No    |
|    6     | C1.1 | C2.3 | C3.1 | C4.2 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    7     | C1.1 | C2.3 | C3.1 | C4.1 | C5.2 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |    No    |
|    8     | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.2 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    9     | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.2 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |    No    |
|    10    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.2 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    11    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.2 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |    No    |
|    12    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.2 | C11.1 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    13    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.2 | C12.1 | C13.1 | C14.1 | C15.1 |   Yes    |
|    14    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.2 | C13.1 | C14.1 | C15.1 |    No    |
|    15    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.2 | C14.1 | C15.1 |   Yes    |
|    16    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.2 | C15.1 |    No    |
|    17    | C1.1 | C2.3 | C3.1 | C4.1 | C5.1 | C6.1 | C7.1 | C8.1 | C9.1 | C10.1 | C11.1 | C12.1 | C13.1 | C14.1 | C15.2 |   Yes    |

**Test set:** 1,3,4,6,8,10,12,13,15,17

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.PetService.getPetById(Long petId)`

* C1: `petId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `petId` value (C2.1 negative / C2.2 zero / C2.3 positive)

|    #     |  C1  |  C2  | Feasible |
|:--------:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 |   Yes    |
|    2     | C1.2 | C2.3 |    No    |
|    3     | C1.1 | C2.1 |   Yes    |
|    4     | C1.1 | C2.2 |   Yes    |

**Test set:** 1,3,4

### Base Choice Coverage (BCC)
**Method**: `com.petify.petify.service.FavoritesService.removeFavorite(Long userId, Long listingId)`

* C1: `userId` is `null` (C1.1 `false` / C1.2 `true`)
* C2: `userId` value (C2.1 negative / C2.2 zero / C2.3 positive)
* C3: `listingId` is `null` (C3.1 `false` / C3.2 `true`)
* C4: `listingId` value (C4.1 negative / C4.2 zero / C4.3 positive)

|    #     |  C1  |  C2  |  C3  |  C4  | Feasible |
|:--------:|:----:|:----:|:----:|:----:|:--------:|
| 1 (base) | C1.1 | C2.3 | C3.1 | C4.3 |   Yes    |
|    2     | C1.2 | C2.3 | C3.1 | C4.3 |    No    |
|    3     | C1.1 | C2.1 | C3.1 | C4.3 |   Yes    |
|    4     | C1.1 | C2.2 | C3.1 | C4.3 |   Yes    |
|    5     | C1.1 | C2.3 | C3.2 | C4.3 |    No    |
|    6     | C1.1 | C2.3 | C3.1 | C4.1 |   Yes    |
|    7     | C1.1 | C2.3 | C3.1 | C4.2 |   Yes    |

**Test set:** 1,3,4,6,7