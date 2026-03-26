// This file intentionally removed.
// AnalyticsEventConsumer handles all queue consumption with proper DB persistence.
// This class was using in-memory counters (not thread-safe, resets on restart) and
// was competing with AnalyticsEventConsumer for the same messages.
package com.capg.analyticsservice.consumer;
