// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

#include "tcir_jit.h"

#include <stdlib.h>

#if defined(_WIN32)
#include <windows.h>
typedef CRITICAL_SECTION TCIRJitMutex;
#define TCIR_JIT_MUTEX_INIT(mutex) (InitializeCriticalSection((mutex)), 1)
#define TCIR_JIT_MUTEX_DESTROY(mutex) DeleteCriticalSection((mutex))
#define TCIR_JIT_MUTEX_LOCK(mutex) EnterCriticalSection((mutex))
#define TCIR_JIT_MUTEX_UNLOCK(mutex) LeaveCriticalSection((mutex))
#else
#include <pthread.h>
typedef pthread_mutex_t TCIRJitMutex;
#define TCIR_JIT_MUTEX_INIT(mutex) (pthread_mutex_init((mutex), NULL) == 0)
#define TCIR_JIT_MUTEX_DESTROY(mutex) ((void)pthread_mutex_destroy((mutex)))
#define TCIR_JIT_MUTEX_LOCK(mutex) ((void)pthread_mutex_lock((mutex)))
#define TCIR_JIT_MUTEX_UNLOCK(mutex) ((void)pthread_mutex_unlock((mutex)))
#endif

typedef enum TCIRJitCacheEntryState
{
   TCIR_JIT_CACHE_ENTRY_COMPILING = 0,
   TCIR_JIT_CACHE_ENTRY_READY,
   TCIR_JIT_CACHE_ENTRY_REJECTED
} TCIRJitCacheEntryState;

typedef struct TCIRJitCacheEntry
{
   const void *method_key;
   TCIRJitCacheEntryState state;
   TCIRJitArtifact *artifact;
   struct TCIRJitCacheEntry *next;
} TCIRJitCacheEntry;

struct TCIRJitCache
{
   TCIRJitMutex mutex;
   TCIRJitCacheEntry *entries;
   size_t active_claim_count;
   int shutdown;
};

struct TCIRJitClaim
{
   TCIRJitCache *cache;
   TCIRJitCacheEntry *entry;
   int active;
};

TCIRJitCache *tcirJitCacheCreate(void)
{
   TCIRJitCache *cache = (TCIRJitCache *)calloc(1U, sizeof(*cache));
   if (cache == NULL)
      return NULL;
   if (!TCIR_JIT_MUTEX_INIT(&cache->mutex))
   {
      free(cache);
      return NULL;
   }
   return cache;
}

static void tcirJitCacheDisposeEntries(TCIRJitCacheEntry *entry)
{
   while (entry != NULL)
   {
      TCIRJitCacheEntry *next = entry->next;
      tcirJitArtifactDestroy(entry->artifact);
      free(entry);
      entry = next;
   }
}

void tcirJitCacheDestroy(TCIRJitCache *cache)
{
   TCIRJitCacheEntry *entries;

   if (cache == NULL)
      return;
   TCIR_JIT_MUTEX_LOCK(&cache->mutex);
   cache->shutdown = 1;
   if (cache->active_claim_count != 0U)
   {
      TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
      return;
   }
   entries = cache->entries;
   cache->entries = NULL;
   TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
   tcirJitCacheDisposeEntries(entries);
   TCIR_JIT_MUTEX_DESTROY(&cache->mutex);
   free(cache);
}

static void tcirJitCacheReleaseClaim(TCIRJitClaim *claim)
{
   TCIRJitCache *cache = claim->cache;
   TCIRJitCacheEntry *entries = NULL;
   int dispose_cache = 0;

   TCIR_JIT_MUTEX_LOCK(&cache->mutex);
   claim->active = 0;
   if (cache->active_claim_count != 0U)
      --cache->active_claim_count;
   if (cache->shutdown && cache->active_claim_count == 0U)
   {
      entries = cache->entries;
      cache->entries = NULL;
      dispose_cache = 1;
   }
   TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
   free(claim);
   if (dispose_cache)
   {
      tcirJitCacheDisposeEntries(entries);
      TCIR_JIT_MUTEX_DESTROY(&cache->mutex);
      free(cache);
   }
}

TCIRJitCacheStatus tcirJitCacheBegin(
   TCIRJitCache *cache,
   const void *method_key,
   const TCIRJitArtifact **artifact,
   TCIRJitClaim **claim,
   TCIRJitDiagnostic *diagnostic)
{
   TCIRJitCacheEntry *entry;
   TCIRJitClaim *created_claim;

   tcirJitDiagnosticClear(diagnostic);
   if (artifact != NULL)
      *artifact = NULL;
   if (claim != NULL)
      *claim = NULL;
   if (cache == NULL || method_key == NULL || artifact == NULL || claim == NULL)
      return TCIR_JIT_CACHE_INVALID_ARGUMENT;

   TCIR_JIT_MUTEX_LOCK(&cache->mutex);
   if (cache->shutdown)
   {
      TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
      return TCIR_JIT_CACHE_SHUTDOWN;
   }
   for (entry = cache->entries; entry != NULL; entry = entry->next)
      if (entry->method_key == method_key)
      {
         TCIRJitCacheStatus status;
         if (entry->state == TCIR_JIT_CACHE_ENTRY_READY)
         {
            *artifact = entry->artifact;
            status = TCIR_JIT_CACHE_READY;
         }
         else if (entry->state == TCIR_JIT_CACHE_ENTRY_COMPILING)
            status = TCIR_JIT_CACHE_COMPILING;
         else
            status = TCIR_JIT_CACHE_REJECTED;
         TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
         return status;
      }

   entry = (TCIRJitCacheEntry *)calloc(1U, sizeof(*entry));
   created_claim = (TCIRJitClaim *)calloc(1U, sizeof(*created_claim));
   if (entry == NULL || created_claim == NULL)
   {
      free(created_claim);
      free(entry);
      TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
      return TCIR_JIT_CACHE_OUT_OF_MEMORY;
   }
   entry->method_key = method_key;
   entry->state = TCIR_JIT_CACHE_ENTRY_COMPILING;
   entry->next = cache->entries;
   cache->entries = entry;
   created_claim->cache = cache;
   created_claim->entry = entry;
   created_claim->active = 1;
   ++cache->active_claim_count;
   *claim = created_claim;
   TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
   return TCIR_JIT_CACHE_CLAIMED;
}

int tcirJitCachePublish(TCIRJitClaim *claim, TCIRJitArtifact *artifact)
{
   TCIRJitCache *cache;
   int published = 0;

   if (claim == NULL || artifact == NULL || !claim->active || claim->cache == NULL || claim->entry == NULL)
      return 0;
   cache = claim->cache;
   TCIR_JIT_MUTEX_LOCK(&cache->mutex);
   if (!cache->shutdown && claim->entry->state == TCIR_JIT_CACHE_ENTRY_COMPILING)
   {
      claim->entry->artifact = artifact;
      claim->entry->state = TCIR_JIT_CACHE_ENTRY_READY;
      published = 1;
   }
   TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
   tcirJitCacheReleaseClaim(claim);
   return published;
}

void tcirJitCacheReject(TCIRJitClaim *claim)
{
   TCIRJitCache *cache;

   if (claim == NULL)
      return;
   cache = claim->cache;
   if (claim->active && cache != NULL && claim->entry != NULL)
   {
      TCIR_JIT_MUTEX_LOCK(&cache->mutex);
      if (!cache->shutdown && claim->entry->state == TCIR_JIT_CACHE_ENTRY_COMPILING)
         claim->entry->state = TCIR_JIT_CACHE_ENTRY_REJECTED;
      TCIR_JIT_MUTEX_UNLOCK(&cache->mutex);
      tcirJitCacheReleaseClaim(claim);
      return;
   }
   free(claim);
}
