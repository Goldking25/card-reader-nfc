package com.nfcpoc.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardDatabase_Impl extends CardDatabase {
  private volatile CardDao _cardDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `nfc_cards` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT NOT NULL, `uidRaw` TEXT NOT NULL, `cardType` TEXT NOT NULL, `label` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `techList` TEXT NOT NULL, `sectors` TEXT NOT NULL, `mifareSize` INTEGER NOT NULL, `mifareType` INTEGER NOT NULL, `authenticatedSectorCount` INTEGER NOT NULL, `pages` TEXT NOT NULL, `ultralightType` INTEGER NOT NULL, `historicalBytes` TEXT, `atqa` TEXT, `sak` INTEGER, `hiLayerResponse` TEXT, `applicationData` TEXT, `protocolInfo` TEXT, `idm` TEXT, `pmm` TEXT, `systemCode` TEXT, `apduLog` TEXT NOT NULL, `selectedAid` TEXT, `notes` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ca6cb53d0b40165cf878764042c15f62')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `nfc_cards`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsNfcCards = new HashMap<String, TableInfo.Column>(25);
        _columnsNfcCards.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("uid", new TableInfo.Column("uid", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("uidRaw", new TableInfo.Column("uidRaw", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("cardType", new TableInfo.Column("cardType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("techList", new TableInfo.Column("techList", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("sectors", new TableInfo.Column("sectors", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("mifareSize", new TableInfo.Column("mifareSize", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("mifareType", new TableInfo.Column("mifareType", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("authenticatedSectorCount", new TableInfo.Column("authenticatedSectorCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("pages", new TableInfo.Column("pages", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("ultralightType", new TableInfo.Column("ultralightType", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("historicalBytes", new TableInfo.Column("historicalBytes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("atqa", new TableInfo.Column("atqa", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("sak", new TableInfo.Column("sak", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("hiLayerResponse", new TableInfo.Column("hiLayerResponse", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("applicationData", new TableInfo.Column("applicationData", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("protocolInfo", new TableInfo.Column("protocolInfo", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("idm", new TableInfo.Column("idm", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("pmm", new TableInfo.Column("pmm", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("systemCode", new TableInfo.Column("systemCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("apduLog", new TableInfo.Column("apduLog", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("selectedAid", new TableInfo.Column("selectedAid", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsNfcCards.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysNfcCards = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesNfcCards = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoNfcCards = new TableInfo("nfc_cards", _columnsNfcCards, _foreignKeysNfcCards, _indicesNfcCards);
        final TableInfo _existingNfcCards = TableInfo.read(db, "nfc_cards");
        if (!_infoNfcCards.equals(_existingNfcCards)) {
          return new RoomOpenHelper.ValidationResult(false, "nfc_cards(com.nfcpoc.data.model.NfcCard).\n"
                  + " Expected:\n" + _infoNfcCards + "\n"
                  + " Found:\n" + _existingNfcCards);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ca6cb53d0b40165cf878764042c15f62", "113a955ed1a49b35743a8475aa9c061e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "nfc_cards");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `nfc_cards`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(CardDao.class, CardDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public CardDao cardDao() {
    if (_cardDao != null) {
      return _cardDao;
    } else {
      synchronized(this) {
        if(_cardDao == null) {
          _cardDao = new CardDao_Impl(this);
        }
        return _cardDao;
      }
    }
  }
}
