<template>
  <div class="profile-container">
    <!-- Back to listings if not authenticated -->
    <div v-if="!auth.isAuthenticated" class="container py-5">
      <div class="alert alert-warning alert-dismissible fade show" role="alert">
        <strong>Not logged in</strong>
        <p class="mb-0">Please <RouterLink to="/login" class="alert-link">log in</RouterLink> to view your profile.</p>
      </div>
    </div>

    <!-- User Info Header -->
    <div v-else class="header-section">
      <div class="container">
        <div class="profile-card">
          <div class="profile-content">
            <div class="profile-info">
              <div style="display: flex; align-items: center; gap: 12px;">
                <h1 class="profile-name">{{ auth.user?.firstName }} {{ auth.user?.lastName }}</h1>
                <span v-if="auth.user?.verified" class="verified-badge">
                  <img src="@/img/star.png" alt="verified" class="badge-star" /> Top 10
                </span>
              </div>
              <p class="profile-username">@{{ auth.user?.username }}</p>
              <p class="profile-email">
                <i class="bi bi-envelope"></i> {{ auth.user?.email }}
              </p>
            </div>
            <div class="profile-badge">
              <span class="badge bg-primary">{{ userType }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tabs for content -->
    <div v-if="auth.isAuthenticated" class="main-content">
      <div class="container">
        <div class="tabs-container">
          <ul class="nav nav-tabs nav-fill" role="tablist">
            <li class="nav-item" role="presentation">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'listings' }"
                @click="activeTab = 'listings'"
                type="button"
                role="tab"
              >
                <i class="bi bi-bookmark-fill"></i> My Listings
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'pets' }"
                @click="activeTab = 'pets'"
                type="button"
                role="tab"
              >
                <i class="bi bi-paw-fill"></i> My Pets
              </button>
            </li>
            <li class="nav-item" role="presentation">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'favorites' }"
                @click="activeTab = 'favorites'"
                type="button"
                role="tab"
              >
                <i class="bi bi-heart-fill"></i> Favorites
              </button>
            </li>
            <li v-if="pets.length > 0" class="nav-item" role="presentation">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'create-listing' }"
                @click="activeTab = 'create-listing'"
                type="button"
                role="tab"
              >
                <i class="bi bi-plus-circle-fill"></i> Create Listing
              </button>
            </li>
            <li v-if="isOwner" class="nav-item" role="presentation">
              <button
                class="nav-link"
                :class="{ active: activeTab === 'appointments' }"
                @click="activeTab = 'appointments'"
                type="button"
                role="tab"
              >
                <i class="bi bi-calendar-check-fill"></i> Appointments
              </button>
            </li>
          </ul>

          <!-- Listings Tab -->
          <div v-if="activeTab === 'listings'" class="tab-content-section">
            <h2 class="section-title">My Listings</h2>
            <div v-if="listings.length === 0" class="empty-state">
              <div class="empty-icon">📋</div>
              <p class="empty-text">You haven't created any listings yet.</p>
              <a href="#" @click.prevent="activeTab = 'create-listing'" class="btn btn-primary btn-sm">
                Create your first listing
              </a>
            </div>
            <div v-else class="grid-container">
              <div v-for="listing in listings" :key="listing.listingId" class="listing-card-wrapper">
                <div class="listing-card">
                  <div class="listing-header">
                    <h5 class="listing-title">{{ getPetName(listing.animalId) }}</h5>
                    <span class="badge" :class="getStatusBadgeClass(listing.status)">
                      {{ listing.status }}
                    </span>
                  </div>
                  <p class="listing-description">{{ listing.description }}</p>
                  <div class="listing-footer">
                    <div class="listing-price">${{ listing.price.toFixed(2) }}</div>
                    <small class="listing-date">{{ formatDate(listing.createdAt) }}</small>
                  </div>
                  <div class="listing-actions">
                    <select
                      v-model="listing.status"
                      @change="updateStatus(listing)"
                      class="form-select form-select-sm"
                    >
                      <option value="DRAFT">Draft</option>
                      <option value="ACTIVE">Active</option>
                      <option value="SOLD">Sold</option>
                      <option value="ARCHIVED">Archived</option>
                    </select>
                    <button
                      @click="deleteListing_(listing.listingId)"
                      class="btn btn-sm btn-danger"
                      title="Delete listing"
                    >
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Pets Tab -->
          <div v-if="activeTab === 'pets'" class="tab-content-section">
            <div class="section-header-row">
              <h2 class="section-title">My Pets</h2>
              <button class="btn btn-primary btn-sm" type="button" @click="openAddPetForm">
                <i class="bi bi-plus-circle-fill"></i> Add Pet
              </button>
            </div>
            <div v-if="pets.length === 0" class="empty-state">
              <img src="@/img/all_outline.png" alt="No pets" class="empty-icon-img" />
              <p class="empty-text">You don't have any pets yet.</p>
              <p class="empty-subtext">Add pets to create listings!</p>
              <a href="#" @click.prevent="openAddPetForm" class="btn btn-primary btn-sm">
                Add your first pet
              </a>
            </div>
            <div v-else class="grid-container">
              <div v-for="pet in pets" :key="pet.animalId" class="pet-card-wrapper">
                <div class="pet-card">
                  <div class="pet-image-wrapper">
                    <img
                      v-if="pet.photoUrl"
                      :src="pet.photoUrl"
                      :alt="pet.name"
                      class="pet-image"
                    />
                    <img v-else src="@/img/all_outline.png" :alt="`${pet.name} placeholder`" class="pet-image-placeholder-img" />
                  </div>
                  <div class="pet-header">
                    <h5 class="pet-name">{{ pet.name }}</h5>
                  </div>
                  <div class="pet-details">
                    <div class="pet-detail-row">
                      <span class="label">Species:</span>
                      <span class="value">{{ pet.species }}</span>
                    </div>
                    <div v-if="pet.type" class="pet-detail-row">
                      <span class="label">Type:</span>
                      <span class="value">{{ pet.type }}</span>
                    </div>
                    <div v-if="pet.breed" class="pet-detail-row">
                      <span class="label">Breed:</span>
                      <span class="value">{{ pet.breed }}</span>
                    </div>
                    <div v-if="pet.sex" class="pet-detail-row">
                      <span class="label">Sex:</span>
                      <span class="value">{{ pet.sex }}</span>
                    </div>
                    <div v-if="pet.dateOfBirth" class="pet-detail-row">
                      <span class="label">DOB:</span>
                      <span class="value">{{ formatDate(pet.dateOfBirth) }}</span>
                    </div>
                    <div v-if="pet.locatedName" class="pet-detail-row">
                      <span class="label">Location:</span>
                      <span class="value">{{ pet.locatedName }}</span>
                    </div>
                  </div>
                  <button
                    v-if="isOwner"
                    @click="selectPetForListing(pet)"
                    class="btn btn-primary btn-sm w-100"
                  >
                    Create Listing for {{ pet.name }}
                  </button>
                  <button
                    v-if="isOwner"
                    @click="selectPetForAppointment(pet)"
                    class="btn btn-outline-secondary btn-sm w-100"
                  >
                    Schedule Appointment for {{ pet.name }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Create Listing Tab -->
          <div v-if="activeTab === 'create-listing'" class="tab-content-section">
            <h2 class="section-title">Create New Listing</h2>
            <div v-if="pets.length === 0" class="empty-state">
              <p>You need to have at least one pet to create a listing.</p>
            </div>
            <div v-else class="form-card">
              <form @submit.prevent="submitListing">
                <div class="form-group">
                  <label for="petSelect" class="form-label">Select Pet *</label>
                  <select
                    v-model.number="newListing.animalId"
                    id="petSelect"
                    class="form-select"
                    required
                  >
                    <option value="">Choose a pet...</option>
                    <option v-for="pet in pets" :key="pet.animalId" :value="pet.animalId">
                      {{ pet.name }} ({{ pet.species }})
                    </option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="description" class="form-label">Description *</label>
                  <textarea
                    v-model="newListing.description"
                    id="description"
                    class="form-control"
                    rows="5"
                    placeholder="Describe your pet and why they're available..."
                    required
                  ></textarea>
                </div>

                <div class="form-group">
                  <label for="price" class="form-label">Price *</label>
                  <div class="input-group">
                    <span class="input-group-text">$</span>
                    <input
                      v-model.number="newListing.price"
                      type="number"
                      id="price"
                      class="form-control"
                      placeholder="0.00"
                      step="0.01"
                      min="0"
                      required
                    />
                  </div>
                </div>

                <div v-if="errorMessage" class="alert alert-danger">
                  {{ errorMessage }}
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="isSubmitting">
                    <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                    {{ isSubmitting ? 'Creating...' : 'Create Listing' }}
                  </button>
                  <button type="button" @click="resetForm" class="btn btn-outline-secondary" :disabled="isSubmitting">
                    Reset
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Add Pet Form -->
          <div v-if="activeTab === 'pets' && showAddPetForm" ref="addPetPanel" class="tab-content-section add-pet-panel">
            <h2 class="section-title">Add New Pet</h2>
            <div class="form-card">
              <form @submit.prevent="submitPet">
                <div class="form-row">
                  <div class="form-group">
                    <label for="petName" class="form-label">Pet Name *</label>
                    <input
                      v-model="newPet.name"
                      type="text"
                      id="petName"
                      class="form-control"
                      placeholder="e.g., Buddy"
                      required
                    />
                  </div>
                  <div class="form-group">
                    <label for="petSex" class="form-label">Sex *</label>
                    <select v-model="newPet.sex" id="petSex" class="form-select" required>
                      <option value="">Select sex...</option>
                      <option value="MALE">Male</option>
                      <option value="FEMALE">Female</option>
                      <option value="UNKNOWN">Unknown</option>
                    </select>
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="petSpecies" class="form-label">Species *</label>
                    <select v-model="newPet.species" id="petSpecies" class="form-select" required>
                      <option value="">Select species...</option>
                      <option value="Dog">Dog</option>
                      <option value="Cat">Cat</option>
                      <option value="Bird">Bird</option>
                      <option value="Rabbit">Rabbit</option>
                      <option value="Hamster">Hamster</option>
                      <option value="Guinea Pig">Guinea Pig</option>
                      <option value="Other">Other</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label for="petBreed" class="form-label">Breed</label>
                    <input
                      v-model="newPet.breed"
                      type="text"
                      id="petBreed"
                      class="form-control"
                      placeholder="e.g., Golden Retriever"
                    />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="petDateOfBirth" class="form-label">Date of Birth</label>
                    <input
                      v-model="newPet.dateOfBirth"
                      type="date"
                      id="petDateOfBirth"
                      class="form-control"
                    />
                  </div>
                  <div class="form-group">
                    <label for="petLocatedName" class="form-label">Location</label>
                    <input
                      v-model="newPet.locatedName"
                      type="text"
                      id="petLocatedName"
                      class="form-control"
                      placeholder="e.g., Skopje"
                    />
                  </div>
                </div>

                <div class="form-group">
                  <label for="petPhoto" class="form-label">Photo</label>
                  <input
                    type="file"
                    id="petPhoto"
                    class="form-control"
                    accept="image/jpeg,image/png,image/webp,image/gif"
                    @change="handlePetPhotoChange"
                  />
                  <small class="text-muted">JPG, PNG, WEBP, or GIF up to 5MB.</small>
                  <div v-if="petPhotoPreview" class="pet-photo-preview">
                    <img :src="petPhotoPreview" alt="Selected pet preview" />
                    <button type="button" class="btn btn-sm btn-outline-secondary" @click="clearPetPhoto">
                      Remove photo
                    </button>
                  </div>
                </div>

                <div v-if="errorMessage" class="alert alert-danger">
                  {{ errorMessage }}
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="isPetSubmitting">
                    <span v-if="isPetSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                    {{ isPetSubmitting ? 'Adding Pet...' : 'Add Pet' }}
                  </button>
                  <button type="button" @click="resetPetForm" class="btn btn-outline-secondary" :disabled="isPetSubmitting">
                    Reset
                  </button>
                  <button type="button" @click="hideAddPetForm" class="btn btn-outline-secondary" :disabled="isPetSubmitting">
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          </div>

          <!-- Favorites Tab -->
          <div v-if="activeTab === 'favorites'" class="tab-content-section">
            <h2 class="section-title">Favorite Listings</h2>
            <div v-if="favorites.length === 0" class="empty-state">
              <div class="empty-icon">♡</div>
              <p class="empty-text">You haven't added any favorites yet.</p>
              <p class="empty-subtext">Browse listings and click the heart icon to save them!</p>
            </div>
            <div v-else class="grid-container">
              <div v-for="listing in favorites" :key="listing.listingId" class="listing-card-wrapper">
                <div
                  class="listing-card favorite-listing"
                  @click="goToListing(listing.listingId)"
                  role="button"
                  tabindex="0"
                  @keydown.enter="goToListing(listing.listingId)"
                >
                  <div class="favorite-image-wrapper">
                    <img
                      :src="getFavoriteListingImage(listing)"
                      :alt="getPetName(listing.animalId)"
                      class="favorite-image"
                      @error="(e) => handleFavoriteImageError(e)"
                    />
                  </div>
                  <div class="favorite-content">
                    <h5 class="listing-title">{{ getPetName(listing.animalId) }}</h5>
                    <span class="badge" :class="getStatusBadgeClass(listing.status)">
                      {{ listing.status }}
                    </span>
                  </div>
                  <p class="listing-description">{{ listing.description }}</p>
                  <div class="listing-footer">
                    <div class="listing-price">${{ listing.price.toFixed(2) }}</div>
                    <small class="listing-date">{{ formatDate(listing.createdAt) }}</small>
                  </div>
                  <button
                    @click.stop="removeFavorite(listing.listingId)"
                    class="btn btn-sm btn-outline-danger w-100"
                  >
                    <i class="bi bi-heart-fill"></i> Remove from Favorites
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Create Appointment Tab -->
          <div v-if="activeTab === 'appointments'" class="tab-content-section">
            <div v-if="appointmentsError" class="alert alert-danger">
              {{ appointmentsError }}
            </div>
            <div v-if="isAppointmentsLoading" class="alert alert-info">Loading appointments...</div>
            <OwnerAppointmentsCalendar
              v-if="!isAppointmentsLoading"
              :appointments="appointments"
              :selected-date="selectedAppointmentDate"
              @select="handleCalendarSelect"
            />
            <div class="appointments-day-section">
              <h3 class="appointments-day-title">
                Appointments for {{ selectedAppointmentDate || 'Select a day' }}
              </h3>
              <div v-if="selectedDayAppointments.length === 0" class="empty-state appointments-empty">
                <p>No appointments scheduled for this day.</p>
              </div>
              <div v-else class="appointments-list">
                <div v-for="appt in selectedDayAppointments" :key="appt.appointmentId" class="appointment-card">
                  <div class="appointment-header">
                    <div class="appointment-title">
                      {{ appt.petName || 'Pet' }}
                      <span v-if="appt.petSpecies">({{ appt.petSpecies }})</span>
                    </div>
                    <span class="badge" :class="getStatusBadgeClass(appt.status)">
                      {{ appt.status }}
                    </span>
                  </div>
                  <div class="appointment-meta">
                    <div class="appointment-time">{{ formatDateTime(appt.dateTime) }}</div>
                    <div class="appointment-clinic">
                      {{ appt.clinicName || 'Clinic' }} - {{ appt.clinicCity || '' }} {{ appt.clinicAddress || '' }}
                    </div>
                  </div>
                  <div v-if="appt.notes" class="appointment-notes">{{ appt.notes }}</div>
                  <div v-if="appointmentError" class="alert alert-danger appointment-inline-error">
                    {{ appointmentError }}
                  </div>
                  <div v-if="canCancelAppointment(appt)" class="appointment-actions">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-danger"
                      :disabled="cancellingAppointmentId === appt.appointmentId"
                      @click="cancelAppointment(appt)"
                    >
                      {{ cancellingAppointmentId === appt.appointmentId ? 'Cancelling...' : 'Cancel appointment' }}
                    </button>
                  </div>
                  <div v-if="appt.status === 'DONE'" class="clinic-review-section">
                    <div v-if="getClinicReview(appt.clinicId) && activeClinicReviewAppointmentId !== appt.appointmentId" class="clinic-review-summary">
                      <div>
                        <div class="clinic-review-stars">
                          <span v-for="i in 5" :key="i" :class="{ active: i <= (getClinicReview(appt.clinicId)?.rating || 0) }">★</span>
                        </div>
                        <p class="clinic-review-comment">{{ getClinicReview(appt.clinicId)?.comment || 'No comment' }}</p>
                      </div>
                      <div class="clinic-review-actions">
                        <button type="button" class="btn btn-sm btn-outline-primary" @click="startClinicReview(appt)">
                          Edit review
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-danger" @click="deleteClinicReview(appt)">
                          Delete
                        </button>
                      </div>
                    </div>

                    <button
                      v-else-if="activeClinicReviewAppointmentId !== appt.appointmentId"
                      type="button"
                      class="btn btn-sm btn-outline-primary"
                      @click="startClinicReview(appt)"
                    >
                      Leave clinic review
                    </button>

                    <form v-else class="clinic-review-form" @submit.prevent="submitClinicReview(appt)">
                      <div class="clinic-review-stars editable">
                        <button
                          v-for="i in 5"
                          :key="i"
                          type="button"
                          :class="{ active: i <= clinicReviewForm.rating }"
                          @click="clinicReviewForm.rating = i"
                        >
                          ★
                        </button>
                      </div>
                      <textarea
                        v-model="clinicReviewForm.comment"
                        class="form-control"
                        rows="3"
                        placeholder="Share how the clinic visit went..."
                      ></textarea>
                      <div v-if="clinicReviewError" class="alert alert-danger">
                        {{ clinicReviewError }}
                      </div>
                      <div class="clinic-review-actions">
                        <button type="submit" class="btn btn-sm btn-primary">
                          {{ getClinicReview(appt.clinicId) ? 'Save review' : 'Submit review' }}
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-secondary" @click="cancelClinicReview">
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                  <div v-if="appt.status === 'DONE'" class="health-record-section">
                    <div v-if="getHealthRecordForAppointment(appt) && activeHealthRecordAppointmentId !== appt.appointmentId" class="health-record-summary">
                      <div>
                        <strong>{{ getHealthRecordForAppointment(appt)?.type }}</strong>
                        <p>{{ getHealthRecordForAppointment(appt)?.description || 'No description' }}</p>
                        <small>{{ formatDate(getHealthRecordForAppointment(appt)?.date || appt.dateTime) }}</small>
                      </div>
                    </div>

                    <button
                      v-else-if="activeHealthRecordAppointmentId !== appt.appointmentId"
                      type="button"
                      class="btn btn-sm btn-outline-success"
                      @click="startHealthRecord(appt)"
                    >
                      Add health record
                    </button>

                    <form v-else class="health-record-form" @submit.prevent="submitHealthRecord(appt)">
                      <div class="form-group">
                        <label class="form-label">Record type *</label>
                        <input
                          v-model="healthRecordForm.type"
                          class="form-control"
                          placeholder="Vaccination, checkup, treatment..."
                          required
                        />
                      </div>
                      <div class="form-group">
                        <label class="form-label">Description</label>
                        <textarea
                          v-model="healthRecordForm.description"
                          class="form-control"
                          rows="3"
                          placeholder="Describe what happened during the appointment..."
                        ></textarea>
                      </div>
                      <div v-if="healthRecordError" class="alert alert-danger">
                        {{ healthRecordError }}
                      </div>
                      <div class="clinic-review-actions">
                        <button type="submit" class="btn btn-sm btn-success" :disabled="isHealthRecordSubmitting">
                          {{ isHealthRecordSubmitting ? 'Saving...' : 'Save health record' }}
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-secondary" @click="cancelHealthRecord">
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              </div>
            </div>
            <h2 class="section-title">Create Appointment</h2>
            <div v-if="pets.length === 0" class="empty-state">
              <p>You need to have at least one pet to create an appointment.</p>
            </div>
            <div v-else class="form-card">
              <form @submit.prevent="submitAppointment">
                <div class="form-group">
                  <label for="appointmentPet" class="form-label">Select Pet *</label>
                  <select
                    v-model.number="newAppointment.animalId"
                    id="appointmentPet"
                    class="form-select"
                    required
                  >
                    <option value="">Choose a pet...</option>
                    <option v-for="pet in pets" :key="pet.animalId" :value="pet.animalId">
                      {{ pet.name }} ({{ pet.species }})
                    </option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="appointmentClinic" class="form-label">Clinic *</label>
                  <select
                    v-model.number="newAppointment.clinicId"
                    id="appointmentClinic"
                    class="form-select"
                    required
                  >
                    <option value="">Choose a clinic...</option>
                    <option v-for="clinic in clinics" :key="clinic.clinicId" :value="clinic.clinicId">
                      {{ clinic.name }} - {{ clinic.city }}, {{ clinic.address }}
                    </option>
                  </select>
                  <small v-if="clinicsError" class="text-danger">{{ clinicsError }}</small>
                </div>

                <div class="form-group">
                  <label for="appointmentDate" class="form-label">Date *</label>
                  <input
                    v-model="appointmentDate"
                    type="date"
                    id="appointmentDate"
                    class="form-control"
                    :min="todayDate"
                    required
                  />
                </div>

                <div class="form-group">
                  <label for="appointmentSlot" class="form-label">Available Time Slot *</label>
                  <select
                    v-model="newAppointment.dateTime"
                    id="appointmentSlot"
                    class="form-select"
                    :disabled="!newAppointment.clinicId || !appointmentDate || isSlotsLoading || availableSlots.length === 0"
                    required
                  >
                    <option value="">{{ appointmentSlotPlaceholder }}</option>
                    <option v-for="slot in availableSlots" :key="slot.dateTime" :value="slot.dateTime">
                      {{ slot.label }}
                    </option>
                  </select>
                  <small v-if="isSlotsLoading" class="text-muted">Loading available slots...</small>
                  <small v-else-if="slotsError" class="text-danger">{{ slotsError }}</small>
                  <small v-else-if="newAppointment.clinicId && appointmentDate && availableSlots.length === 0" class="text-muted">
                    No available slots for this clinic on the selected date.
                  </small>
                </div>

                <div class="form-group">
                  <label for="appointmentNotes" class="form-label">Notes</label>
                  <textarea
                    v-model="newAppointment.notes"
                    id="appointmentNotes"
                    class="form-control"
                    rows="4"
                    placeholder="Add notes for the clinic..."
                  ></textarea>
                </div>

                <div v-if="appointmentError" class="alert alert-danger">
                  {{ appointmentError }}
                </div>

                <div class="form-actions">
                  <button type="submit" class="btn btn-primary" :disabled="isAppointmentSubmitting">
                    <span v-if="isAppointmentSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                    {{ isAppointmentSubmitting ? 'Creating...' : 'Create Appointment' }}
                  </button>
                  <button type="button" @click="resetAppointmentForm" class="btn btn-outline-secondary" :disabled="isAppointmentSubmitting">
                    Reset
                  </button>
                </div>
              </form>
            </div>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onBeforeUnmount, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import OwnerAppointmentsCalendar from '../components/OwnerAppointmentsCalendar.vue'
import {
  getUserListings,
  getUserPets,
  createListing,
  createPet,
  deleteListing,
  updateListingStatus,
  getPet,
  loadUserVerificationStatus,
  createAppointment,
  cancelOwnerAppointment,
  createHealthRecord,
  getClinics,
  getClinicAvailableSlots,
  getPetHealthRecords,
  getOwnerAppointments,
  type HealthRecord,
} from '../api/profile'
import { getFavoritedListings, removeFavorite as removeFavoriteAPI } from '../api/favorites'
import {
  createClinicReview,
  deleteReview as deleteReviewAPI,
  getMyClinicReview,
  updateReview as updateReviewAPI,
  type Review,
} from '../api/reviews'

const router = useRouter()
const auth = useAuthStore()

const activeTab = ref<'listings' | 'pets' | 'create-listing' | 'favorites' | 'appointments'>('listings')
const listings = ref<any[]>([])
const pets = ref<any[]>([])
const favorites = ref<any[]>([])
const clinics = ref<any[]>([])
const availableSlots = ref<any[]>([])
const appointments = ref<any[]>([])
const clinicReviews = ref<Record<number, Review | null>>({})
const healthRecordsByPet = ref<Record<number, HealthRecord[]>>({})
const selectedAppointmentDate = ref('')
const appointmentDate = ref('')
const activeClinicReviewAppointmentId = ref<number | null>(null)
const activeHealthRecordAppointmentId = ref<number | null>(null)
const appointmentsError = ref('')
const isLoading = ref(false)
const isSubmitting = ref(false)
const isPetSubmitting = ref(false)
const isAppointmentSubmitting = ref(false)
const cancellingAppointmentId = ref<number | null>(null)
const isAppointmentsLoading = ref(false)
const isHealthRecordSubmitting = ref(false)
const isSlotsLoading = ref(false)
const errorMessage = ref('')
const appointmentError = ref('')
const clinicsError = ref('')
const slotsError = ref('')
const clinicReviewError = ref('')
const healthRecordError = ref('')
const showAddPetForm = ref(false)
const addPetPanel = ref<HTMLElement | null>(null)
const petPhotoFile = ref<File | null>(null)
const petPhotoPreview = ref('')

const newListing = ref({
  animalId: null as number | null,
  description: '',
  price: null as number | null,
})

const newPet = ref({
  name: '',
  sex: '',
  dateOfBirth: '',
  type: 'PET',
  species: '',
  breed: '',
  locatedName: '',
})

const newAppointment = ref({
  clinicId: null as number | null,
  animalId: null as number | null,
  dateTime: '',
  notes: '',
})

const clinicReviewForm = ref({
  rating: 0,
  comment: '',
})

const healthRecordForm = ref({
  type: '',
  description: '',
})

const isOwner = computed(() => {
  return auth.user?.userType === 'OWNER'
})

const userType = computed(() => {
  return auth.user?.userType || 'Unknown'
})

const todayDate = computed(() => toDateKey(new Date()))

const appointmentSlotPlaceholder = computed(() => {
  if (!newAppointment.value.clinicId) return 'Choose a clinic first...'
  if (!appointmentDate.value) return 'Choose a date first...'
  if (isSlotsLoading.value) return 'Loading slots...'
  if (availableSlots.value.length === 0) return 'No available slots'
  return 'Choose a time slot...'
})

// Store pet details cache for images
const petDetailsCache = ref<Map<number, any>>(new Map())

// Create a map of petId to pet name
const petNameMap = computed(() => {
  const map: Record<number, string> = {}
  // Add pets from the pets list
  pets.value.forEach((pet) => {
    map[pet.animalId] = pet.name
  })
  // Add pets from cache (favorites)
  petDetailsCache.value.forEach((pet, animalId) => {
    map[animalId] = pet.name
  })
  return map
})

// Get pet name for listing
function getPetName(animalId: number): string {
  // Check cache first (from favorites loading)
  if (petDetailsCache.value.has(animalId)) {
    return petDetailsCache.value.get(animalId)?.name || 'Unknown Pet'
  }
  // Fall back to petNameMap (from pets list)
  return petNameMap.value[animalId] || 'Unknown Pet'
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function formatDateTime(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function getStatusBadgeClass(status: string): string {
  switch (status) {
    case 'ACTIVE':
    case 'CONFIRMED':
    case 'DONE':
      return 'bg-success'
    case 'INACTIVE':
    case 'CANCELLED':
    case 'CANCELED':
    case 'NO_SHOW':
      return 'bg-secondary'
    case 'SOLD':
      return 'bg-danger'
    default:
      return 'bg-warning'
  }
}

async function loadListings() {
  if (!auth.user?.userId || !isOwner.value) {
    listings.value = []
    return
  }
  try {
    isLoading.value = true
    listings.value = await getUserListings(auth.user.userId)
  } catch (error) {
    console.error('Failed to load listings:', error)
  } finally {
    isLoading.value = false
  }
}

async function loadPets() {
  if (!auth.user?.userId) return
  try {
    isLoading.value = true
    pets.value = await getUserPets(auth.user.userId)
  } catch (error) {
    console.error('Failed to load pets:', error)
    // If no pets exist, just show empty list
    pets.value = []
  } finally {
    isLoading.value = false
  }
}

async function loadClinics() {
  try {
    clinics.value = await getClinics()
    clinicsError.value = ''
  } catch (error) {
    clinics.value = []
    clinicsError.value = error instanceof Error ? error.message : 'Failed to load clinics'
  }
}

async function loadAppointments() {
  if (!auth.user?.userId || !isOwner.value) return
  try {
    isAppointmentsLoading.value = true
    appointments.value = await getOwnerAppointments(auth.user.userId)
    if (!selectedAppointmentDate.value) {
      selectedAppointmentDate.value = toDateKey(new Date())
    }
    appointmentsError.value = ''
  } catch (error) {
    appointments.value = []
    appointmentsError.value = error instanceof Error ? error.message : 'Failed to load appointments'
  } finally {
    isAppointmentsLoading.value = false
  }

  await loadClinicReviewsForDoneAppointments()
  await loadHealthRecordsForAppointmentPets()
}

async function loadClinicReviewsForDoneAppointments() {
  if (!auth.user?.userId) return

  const clinicIds = Array.from(new Set(
    appointments.value
      .filter((appt) => appt.status === 'DONE' && appt.clinicId)
      .map((appt) => Number(appt.clinicId))
  ))

  const nextReviews: Record<number, Review | null> = {}
  await Promise.all(clinicIds.map(async (clinicId) => {
    try {
      nextReviews[clinicId] = await getMyClinicReview(clinicId, auth.user!.userId)
    } catch (error) {
      console.error(`Failed to load clinic review for clinic ${clinicId}:`, error)
      nextReviews[clinicId] = null
    }
  }))
  clinicReviews.value = nextReviews
}

async function loadHealthRecordsForAppointmentPets() {
  const petIds = Array.from(new Set(
    appointments.value
      .filter((appt) => appt.animalId)
      .map((appt) => Number(appt.animalId))
  ))

  const nextRecords: Record<number, HealthRecord[]> = {}
  await Promise.all(petIds.map(async (petId) => {
    try {
      nextRecords[petId] = await getPetHealthRecords(petId)
    } catch (error) {
      console.error(`Failed to load health records for pet ${petId}:`, error)
      nextRecords[petId] = []
    }
  }))
  healthRecordsByPet.value = nextRecords
}

async function loadAvailableSlots() {
  newAppointment.value.dateTime = ''
  availableSlots.value = []
  slotsError.value = ''

  if (!newAppointment.value.clinicId || !appointmentDate.value) {
    return
  }

  try {
    isSlotsLoading.value = true
    availableSlots.value = await getClinicAvailableSlots(newAppointment.value.clinicId, appointmentDate.value)
  } catch (error) {
    availableSlots.value = []
    slotsError.value = error instanceof Error ? error.message : 'Failed to load available slots'
  } finally {
    isSlotsLoading.value = false
  }
}

function toDateKey(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectedDayAppointments = computed(() => {
  if (!selectedAppointmentDate.value) return []
  return appointments.value.filter((appt) => toDateKey(new Date(appt.dateTime)) === selectedAppointmentDate.value)
})

function handleCalendarSelect(dateKey: string) {
  selectedAppointmentDate.value = dateKey
}

function canCancelAppointment(appt: any): boolean {
  return appt.status === 'CONFIRMED' && new Date(appt.dateTime).getTime() > Date.now()
}

async function cancelAppointment(appt: any) {
  if (!auth.user?.userId) {
    appointmentError.value = 'Please log in to cancel an appointment'
    return
  }

  if (!confirm('Cancel this appointment? The clinic will be notified.')) {
    return
  }

  try {
    cancellingAppointmentId.value = appt.appointmentId
    appointmentError.value = ''
    const updatedAppointment = await cancelOwnerAppointment(auth.user.userId, appt.appointmentId)
    appointments.value = appointments.value.map((appointment) =>
      appointment.appointmentId === updatedAppointment.appointmentId ? updatedAppointment : appointment
    )
    const cancelledDate = String(appt.dateTime || '').slice(0, 10)
    if (
      Number(newAppointment.value.clinicId) === Number(appt.clinicId) &&
      appointmentDate.value === cancelledDate
    ) {
      await loadAvailableSlots()
    }
  } catch (error) {
    appointmentError.value = error instanceof Error ? error.message : 'Failed to cancel appointment'
  } finally {
    cancellingAppointmentId.value = null
  }
}

function getClinicReview(clinicId: number): Review | null {
  return clinicReviews.value[clinicId] || null
}

function startClinicReview(appt: any) {
  const existingReview = getClinicReview(appt.clinicId)
  activeClinicReviewAppointmentId.value = appt.appointmentId
  clinicReviewError.value = ''
  clinicReviewForm.value = {
    rating: existingReview?.rating || 0,
    comment: existingReview?.comment || '',
  }
}

function cancelClinicReview() {
  activeClinicReviewAppointmentId.value = null
  clinicReviewForm.value = {
    rating: 0,
    comment: '',
  }
  clinicReviewError.value = ''
}

async function submitClinicReview(appt: any) {
  if (!auth.user?.userId) {
    clinicReviewError.value = 'Please log in to review this clinic'
    return
  }

  if (clinicReviewForm.value.rating < 1) {
    clinicReviewError.value = 'Please select a rating'
    return
  }

  try {
    const existingReview = getClinicReview(appt.clinicId)
    const savedReview = existingReview
      ? await updateReviewAPI(existingReview.reviewId, auth.user.userId, clinicReviewForm.value.rating, clinicReviewForm.value.comment)
      : await createClinicReview(appt.clinicId, auth.user.userId, clinicReviewForm.value.rating, clinicReviewForm.value.comment)

    clinicReviews.value = {
      ...clinicReviews.value,
      [appt.clinicId]: savedReview,
    }
    cancelClinicReview()
  } catch (error) {
    clinicReviewError.value = error instanceof Error ? error.message : 'Failed to save clinic review'
  }
}

async function deleteClinicReview(appt: any) {
  if (!auth.user?.userId) {
    clinicReviewError.value = 'Please log in to delete this review'
    return
  }

  const existingReview = getClinicReview(appt.clinicId)
  if (!existingReview) return
  if (!confirm('Delete your review for this clinic?')) return

  try {
    await deleteReviewAPI(existingReview.reviewId, auth.user.userId)
    clinicReviews.value = {
      ...clinicReviews.value,
      [appt.clinicId]: null,
    }
    cancelClinicReview()
  } catch (error) {
    clinicReviewError.value = error instanceof Error ? error.message : 'Failed to delete clinic review'
  }
}

function getHealthRecordForAppointment(appt: any): HealthRecord | null {
  if (!appt.animalId) return null
  return (healthRecordsByPet.value[Number(appt.animalId)] || [])
    .find((record) => record.appointmentId === appt.appointmentId) || null
}

function startHealthRecord(appt: any) {
  activeHealthRecordAppointmentId.value = appt.appointmentId
  healthRecordError.value = ''
  healthRecordForm.value = {
    type: '',
    description: '',
  }
}

function cancelHealthRecord() {
  activeHealthRecordAppointmentId.value = null
  healthRecordError.value = ''
  healthRecordForm.value = {
    type: '',
    description: '',
  }
}

async function submitHealthRecord(appt: any) {
  if (!auth.user?.userId) {
    healthRecordError.value = 'Please log in to add a health record'
    return
  }

  if (!healthRecordForm.value.type.trim()) {
    healthRecordError.value = 'Please enter the health record type'
    return
  }

  try {
    isHealthRecordSubmitting.value = true
    healthRecordError.value = ''
    const saved = await createHealthRecord(auth.user.userId, {
      appointmentId: appt.appointmentId,
      type: healthRecordForm.value.type.trim(),
      description: healthRecordForm.value.description || undefined,
    })

    const petId = Number(saved.animalId)
    healthRecordsByPet.value = {
      ...healthRecordsByPet.value,
      [petId]: [
        saved,
        ...(healthRecordsByPet.value[petId] || []).filter((record) => record.healthRecordId !== saved.healthRecordId),
      ],
    }
    cancelHealthRecord()
  } catch (error) {
    healthRecordError.value = error instanceof Error ? error.message : 'Failed to add health record'
  } finally {
    isHealthRecordSubmitting.value = false
  }
}

async function submitListing() {
  if (!auth.user?.userId || !newListing.value.animalId || !newListing.value.price) {
    errorMessage.value = 'Please fill in all required fields'
    return
  }

  try {
    isSubmitting.value = true
    errorMessage.value = ''

    await createListing(auth.user.userId, {
      animalId: newListing.value.animalId,
      description: newListing.value.description,
      price: newListing.value.price,
    })

    // Reload listings
    await loadListings()
    resetForm()
    activeTab.value = 'listings'
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to create listing'
  } finally {
    isSubmitting.value = false
  }
}

async function updateStatus(listing: any) {

  if (!auth.user?.userId) {
    return
  }

  try {
    await updateListingStatus(auth.user.userId, listing.listingId, listing.status)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to update listing'
    await loadListings()
  }
}

async function deleteListing_(listingId: number) {
  if (!auth.user?.userId) return

  if (!confirm('Are you sure you want to delete this listing?')) return

  try {
    await deleteListing(auth.user.userId, listingId)
    await loadListings()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to delete listing'
  }
}

function selectPetForListing(pet: any) {
  newListing.value.animalId = pet.animalId
  activeTab.value = 'create-listing'
}

function selectPetForAppointment(pet: any) {
  newAppointment.value.animalId = pet.animalId
  activeTab.value = 'appointments'
}

function resetForm() {
  newListing.value = {
    animalId: null,
    description: '',
    price: null,
  }
  errorMessage.value = ''
}

function resetAppointmentForm() {
  newAppointment.value = {
    clinicId: null,
    animalId: null,
    dateTime: '',
    notes: '',
  }
  appointmentDate.value = ''
  availableSlots.value = []
  appointmentError.value = ''
  slotsError.value = ''
}

function clearPetPhoto() {
  if (petPhotoPreview.value) {
    URL.revokeObjectURL(petPhotoPreview.value)
  }
  petPhotoPreview.value = ''
  petPhotoFile.value = null

  const input = document.getElementById('petPhoto') as HTMLInputElement | null
  if (input) {
    input.value = ''
  }
}

function handlePetPhotoChange(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] || null

  if (!file) {
    clearPetPhoto()
    return
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif']
  if (!allowedTypes.includes(file.type)) {
    errorMessage.value = 'Pet photo must be a JPG, PNG, WEBP, or GIF image'
    clearPetPhoto()
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = 'Pet photo must be 5MB or smaller'
    clearPetPhoto()
    return
  }

  if (petPhotoPreview.value) {
    URL.revokeObjectURL(petPhotoPreview.value)
  }
  errorMessage.value = ''
  petPhotoFile.value = file
  petPhotoPreview.value = URL.createObjectURL(file)
}

function resetPetForm() {
  newPet.value = {
    name: '',
    sex: '',
    dateOfBirth: '',
    type: 'PET',
    species: '',
    breed: '',
    locatedName: '',
  }
  clearPetPhoto()
  errorMessage.value = ''
}

async function openAddPetForm() {
  showAddPetForm.value = true
  await nextTick()
  addPetPanel.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function hideAddPetForm() {
  resetPetForm()
  showAddPetForm.value = false
}

async function submitPet() {
  if (!auth.user?.userId || !newPet.value.name || !newPet.value.sex || !newPet.value.type || !newPet.value.species) {
    errorMessage.value = 'Please fill in pet name, sex, type, and species'
    return
  }

  try {
    isPetSubmitting.value = true
    errorMessage.value = ''

    const petPayload = {
      name: newPet.value.name,
      sex: newPet.value.sex,
      dateOfBirth: newPet.value.dateOfBirth || undefined,
      photo: petPhotoFile.value || undefined,
      type: newPet.value.type,
      species: newPet.value.species,
      breed: newPet.value.breed || undefined,
      locatedName: newPet.value.locatedName || undefined,
    }

    console.log('📤 Sending pet payload:', petPayload)
    console.log('User ID:', auth.user.userId)

    await createPet(auth.user.userId, petPayload)

    // Reload pets and promote user to owner if needed
    await loadPets()
    resetPetForm()
    showAddPetForm.value = false
    activeTab.value = 'pets'
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to add pet'
  } finally {
    isPetSubmitting.value = false
  }
}

async function submitAppointment() {
  if (!auth.user?.userId || !newAppointment.value.animalId || !newAppointment.value.clinicId || !newAppointment.value.dateTime) {
    appointmentError.value = 'Please fill in clinic, pet, and date/time'
    return
  }

  try {
    isAppointmentSubmitting.value = true
    appointmentError.value = ''

    await createAppointment(auth.user.userId, {
      clinicId: newAppointment.value.clinicId,
      animalId: newAppointment.value.animalId,
      dateTime: newAppointment.value.dateTime,
      notes: newAppointment.value.notes || undefined,
    })

    await loadAppointments()
    resetAppointmentForm()
    activeTab.value = 'pets'
  } catch (error) {
    appointmentError.value = error instanceof Error ? error.message : 'Failed to create appointment'
  } finally {
    isAppointmentSubmitting.value = false
  }
}

async function loadFavorites() {
  if (!auth.user?.userId) return
  try {
    isLoading.value = true
    const favoritesData = await getFavoritedListings(auth.user.userId)

    // Fetch pet images for each favorite listing
    const favoritesWithImages = await Promise.all(
      favoritesData.map(async (listing) => {
        try {
          if (listing.animalId) {
            const pet = await getPet(listing.animalId)
            petDetailsCache.value.set(listing.animalId, pet)
            return {
              ...listing,
              imageUrl: pet.photoUrl || new URL('../img/all_outline.png', import.meta.url).href,
            }
          }
        } catch (err) {
          console.error(`Failed to fetch pet ${listing.animalId}:`, err)
        }
        return {
          ...listing,
          imageUrl: new URL('../img/all_outline.png', import.meta.url).href,
        }
      })
    )

    favorites.value = favoritesWithImages
  } catch (error) {
    console.error('Failed to load favorites:', error)
    favorites.value = []
  } finally {
    isLoading.value = false
  }
}

async function removeFavorite(listingId: number) {
  if (!auth.user?.userId) return

  try {
    await removeFavoriteAPI(auth.user.userId, listingId)
    await loadFavorites()
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Failed to remove favorite'
  }
}

function getFavoriteListingImage(listing: any): string {
  return listing.imageUrl || new URL('../img/all_outline.png', import.meta.url).href
}

function handleFavoriteImageError(e: Event) {
  const img = e.target as HTMLImageElement
  img.src = new URL('../img/all_outline.png', import.meta.url).href
}

function goToListing(listingId: number) {
  router.push({ name: 'listing-details', params: { id: listingId } })
}


async function loadUserVerification() {
  if (!auth.user?.userId) return

  try {
    const isVerified = await loadUserVerificationStatus(auth.user.userId)
    if (auth.user) {
      auth.user.verified = isVerified
    }
    console.log(`✅ User verification status loaded: ${isVerified}`)
  } catch (error) {
    console.error('Failed to load user verification status:', error)
  }
}

onMounted(() => {
  if (!auth.isAuthenticated) {
    router.push('/login')
    return
  }

  loadListings()
  loadPets()
  loadFavorites()
  loadUserVerification()
  loadClinics()
  loadAppointments()

})

onBeforeUnmount(() => {
  if (petPhotoPreview.value) {
    URL.revokeObjectURL(petPhotoPreview.value)
  }
})


watch([() => newAppointment.value.clinicId, appointmentDate], () => {
  loadAvailableSlots()
})
</script>

<style scoped>
.profile-container {
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f3f8 100%);
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Header Section */
.header-section {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  padding: 40px 0;
  margin-bottom: 40px;
  box-shadow: 0 10px 30px rgba(249, 115, 22, 0.2);
}

.profile-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.profile-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 30px;
}

.profile-info {
  flex: 1;
}

.profile-name {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 12px 0;
  letter-spacing: -0.5px;
}

.profile-username {
  font-size: 1.1rem;
  color: #718096;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.profile-email {
  font-size: 1rem;
  color: #4a5568;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-badge {
  display: flex;
  align-items: center;
}

.profile-badge .badge {
  font-size: 0.95rem;
  padding: 8px 16px;
  border-radius: 8px;
}

.verified-badge {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 6px;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.badge-star {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

.badge-star {
  width: 18px;
  height: 18px;
  object-fit: contain;
  filter: brightness(0) invert(1);
}

/* Main Content */
.main-content {
  padding: 0;
}

.tabs-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* Tabs */
.nav-tabs {
  border-bottom: 2px solid #e2e8f0;
  background: #f7fafc;
  padding: 0;
  margin: 0;
}

.nav-tabs .nav-link {
  color: #718096;
  border: none;
  border-bottom: 3px solid transparent;
  font-weight: 600;
  padding: 16px 24px;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.95rem;
}

.nav-tabs .nav-link:hover {
  color: #2d3748;
  background: #edf2f7;
}

.nav-tabs .nav-link.active {
  color: #f97316;
  border-bottom-color: #f97316;
  background: white;
}

/* Tab Content */
.tab-content-section {
  padding: 40px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 30px 0;
  letter-spacing: -0.5px;
}

.section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 30px;
}

.section-header-row .section-title {
  margin-bottom: 0;
}

.add-pet-panel {
  border-top: 1px solid #e2e8f0;
  padding-top: 32px;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 60px 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f3f8 100%);
  border-radius: 12px;
  border: 2px dashed #cbd5e0;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
}

.empty-icon-img {
  width: 120px;
  height: 120px;
  object-fit: contain;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 1.2rem;
  color: #2d3748;
  margin: 0 0 8px 0;
  font-weight: 600;
}

.empty-subtext {
  font-size: 0.95rem;
  color: #718096;
  margin: 0 0 20px 0;
}

/* Grid Container */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

/* Listing Card */
.listing-card-wrapper {
  height: 100%;
}

.listing-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s ease;
  position: relative;
}

.listing-card:hover {
  border-color: #f97316;
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.15);
  transform: translateY(-4px);
}

.favorite-listing .listing-description {
  padding: 0 20px;
}

.favorite-listing .listing-footer {
  padding: 12px 20px;
}

.favorite-listing .btn {
  margin: 0 20px 20px 20px;
}

.favorite-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #f56565;
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
}

.listing-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.listing-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

.listing-description {
  color: #4a5568;
  font-size: 0.95rem;
  line-height: 1.6;
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.listing-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.listing-price {
  font-size: 1.3rem;
  font-weight: 700;
  color: #f97316;
}

.listing-date {
  color: #a0aec0;
  font-size: 0.85rem;
}

.listing-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.listing-actions .form-select {
  flex: 1;
}

/* Pet Card */
.pet-card-wrapper {
  height: 100%;
}

.pet-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
}

.pet-card:hover {
  border-color: #f97316;
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.15);
  transform: translateY(-4px);
}

.pet-image-wrapper {
  width: 100%;
  height: 220px;
  background: linear-gradient(135deg, #f5f7fa 0%, #f0f3f8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.pet-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-image-placeholder-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pet-header {
  border-bottom: none;
  padding: 0 20px 0 20px;
  padding-top: 16px;
}

.pet-name {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
  line-height: 1.4;
}

.pet-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  padding: 0 20px;
}

.pet-detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: none;
  font-size: 0.9rem;
}

.pet-detail-row:last-child {
  border-bottom: none;
}

.pet-detail-row .label {
  color: #718096;
  font-weight: 600;
  text-transform: capitalize;
}

.pet-detail-row .value {
  color: #2d3748;
  font-weight: 500;
}

.pet-card .btn {
  margin: 0 20px 20px 20px;
}

/* Form Card */
.form-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 600;
  color: #2d3748;
  font-size: 0.95rem;
}

.form-control,
.form-select {
  border: 1.5px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.form-control:focus,
.form-select:focus {
  border-color: #f97316;
  box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.1);
}

.pet-photo-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.pet-photo-preview img {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  object-fit: cover;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

/* Buttons */
.btn {
  border-radius: 8px;
  font-weight: 600;
  padding: 10px 20px;
  transition: all 0.2s ease;
  font-size: 0.95rem;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  border: none;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(249, 115, 22, 0.3);
}

.btn-outline-secondary {
  border: 1.5px solid #cbd5e0;
  color: #4a5568;
  background: transparent;
}

.btn-outline-secondary:hover {
  border-color: #2d3748;
  background: #edf2f7;
}

.btn-outline-danger {
  border: 1.5px solid #f56565;
  color: #f56565;
}

.btn-outline-danger:hover {
  background: #fff5f5;
  border-color: #e53e3e;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 0.85rem;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Favorite Listing Card */
.favorite-listing {
  border: none;
  padding: 0;
  overflow: hidden;
}

.appointments-day-section {
  margin: 24px 0 32px;
}

.appointments-day-title {
  font-size: 1.1rem;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1a202c;
}

.appointments-list {
  display: grid;
  gap: 12px;
}

.appointment-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.appointment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.appointment-title {
  font-weight: 700;
  color: #1a202c;
}

.appointment-meta {
  color: #4a5568;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.appointment-notes {
  font-size: 0.9rem;
  color: #2d3748;
  background: #f7fafc;
  padding: 8px 12px;
  border-radius: 8px;
}

.appointment-inline-error {
  margin: 4px 0 0;
}

.appointment-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.clinic-review-section {
  border-top: 1px solid #e2e8f0;
  margin-top: 6px;
  padding-top: 12px;
}

.health-record-section {
  border-top: 1px solid #e2e8f0;
  margin-top: 6px;
  padding-top: 12px;
}

.health-record-summary {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  color: #166534;
  padding: 12px;
}

.health-record-summary p {
  color: #2f6f45;
  margin: 4px 0;
}

.health-record-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.clinic-review-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.clinic-review-stars {
  color: #cbd5e0;
  font-size: 1.15rem;
  line-height: 1;
}

.clinic-review-stars .active,
.clinic-review-stars.editable button.active {
  color: #f97316;
}

.clinic-review-stars.editable {
  display: flex;
  gap: 4px;
}

.clinic-review-stars.editable button {
  background: transparent;
  border: 0;
  color: #cbd5e0;
  cursor: pointer;
  font-size: 1.4rem;
  line-height: 1;
  padding: 0 2px;
}

.clinic-review-comment {
  color: #4a5568;
  font-size: 0.92rem;
  margin: 6px 0 0;
}

.clinic-review-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.clinic-review-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.appointments-empty {
  padding: 24px;
}

/* Badges */
.badge {
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 4px 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.bg-success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
}

.bg-danger {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
}

.bg-secondary {
  background: linear-gradient(135deg, #cbd5e0 0%, #a0aec0 100%);
}

/* Responsive */
@media (max-width: 768px) {
  .header-section {
    padding: 24px 0;
  }

  .profile-card {
    padding: 20px;
  }

  .profile-content {
    flex-direction: column;
  }

  .profile-name {
    font-size: 1.75rem;
  }

  .nav-tabs .nav-link {
    padding: 12px 16px;
    font-size: 0.85rem;
  }

  .tab-content-section {
    padding: 24px;
  }

  .section-title {
    font-size: 1.4rem;
    margin-bottom: 20px;
  }

  .section-header-row {
    align-items: stretch;
    flex-direction: column;
  }

  .grid-container {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .form-card {
    padding: 20px;
  }

  .listing-actions {
    flex-direction: column;
  }

  .listing-actions .form-select {
    width: 100%;
  }
}

@media (max-width: 576px) {
  .profile-name {
    font-size: 1.4rem;
  }

  .section-title {
    font-size: 1.2rem;
  }

  .nav-tabs {
    flex-wrap: wrap;
  }

  .nav-tabs .nav-link {
    flex: 1;
    padding: 8px 12px;
    font-size: 0.8rem;
  }

  .tab-content-section {
    padding: 16px;
  }

  .empty-state {
    padding: 40px 20px;
  }

  .empty-icon {
    font-size: 2.5rem;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .btn {
    width: 100%;
  }
}

/* Admin Panel Styles */
.admin-panel {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.admin-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.admin-subtitle {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.admin-section-header,
.admin-pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.admin-section-header {
  margin-bottom: 16px;
}

.admin-section-header .admin-subtitle {
  margin-bottom: 4px;
}

.admin-page-meta {
  color: #718096;
  font-size: 0.9rem;
  margin: 0;
}

.admin-pagination {
  border-top: 1px solid #e2e8f0;
  color: #4a5568;
  margin-top: 16px;
  padding-top: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  color: white;
  padding: 20px;
  border-radius: 10px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.2);
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
}

.admin-table-container {
  overflow-x: auto;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.95rem;
}

.admin-table thead {
  background: #f7fafc;
  border-bottom: 2px solid #e2e8f0;
}

.admin-table thead th {
  padding: 12px 16px;
  text-align: left;
  font-weight: 600;
  color: #2d3748;
}

.admin-table tbody td {
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  color: #4a5568;
}

.admin-table tbody tr:hover {
  background: #f7fafc;
}

.owner-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.owner-name {
  font-weight: 600;
  color: #2d3748;
}

.owner-username {
  font-size: 0.85rem;
  color: #718096;
}

.badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
}

.bg-success {
  background-color: #c6f6d5;
  color: #22543d;
}

.bg-danger {
  background-color: #fed7d7;
  color: #742a2a;
}

.bg-primary {
  background-color: #bee3f8;
  color: #2c5282;
}

.bg-info {
  background-color: #b2e0d8;
  color: #234e52;
}

.bg-secondary {
  background-color: #cbd5e0;
  color: #2d3748;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .admin-table {
    font-size: 0.85rem;
  }

  .admin-table thead th,
  .admin-table tbody td {
    padding: 8px 12px;
  }
}
</style>
