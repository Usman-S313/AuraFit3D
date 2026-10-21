package com.aurafit3d

import android.app.Application

/**
 * AuraFit3D Application class.
 *
 * Per SRS NFR-5: no third-party analytics in v1.0.
 * Per SRS FR-7.3: health data excluded from backup by default
 *   (handled in AndroidManifest.xml and res/xml/data_extraction_rules.xml).
 *
 * No-op in v1.0 — kept as the application entry point so future initialization
 * (crash reporting, datastore, room) can hook here without touching the manifest again.
 */
class AuraFitApplication : Application()
