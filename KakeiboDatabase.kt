package com.kakeibo.personal.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName="expenses")
data class Expense(@PrimaryKey(autoGenerate=true) val id:Long=0,val date:String,val detail:String,val category:String,val payment:String,val amount:Double,val note:String="")
@Entity(tableName="settings")
data class Budget(@PrimaryKey val month:String,val income:Double=0.0,val extraIncome:Double=0.0,val savingTarget:Double=0.0,val fixedBudget:Double=603.0,val needs:Double=250.0,val wants:Double=120.0,val culture:Double=50.0,val extra:Double=80.0)
@Entity(tableName="shopping")
data class ShoppingItem(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val checked:Boolean=false,val note:String="")
@Entity(tableName="reflections")
data class Reflection(@PrimaryKey val month:String,val q1:String="",val q2:String="",val q3:String="",val q4:String="",val actions:String="")

@Dao interface ExpenseDao { @Query("SELECT * FROM expenses ORDER BY date DESC,id DESC") fun all():Flow<List<Expense>>; @Insert suspend fun insert(e:Expense); @Delete suspend fun delete(e:Expense); @Query("DELETE FROM expenses") suspend fun clear(); @Query("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE category=:category") fun totalByCategory(category:String):Flow<Double>; }
@Dao interface BudgetDao { @Query("SELECT * FROM settings WHERE month=:month") fun get(month:String):Flow<Budget?>; @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(b:Budget); }
@Dao interface ShoppingDao { @Query("SELECT * FROM shopping ORDER BY checked,name") fun all():Flow<List<ShoppingItem>>; @Insert suspend fun insert(i:ShoppingItem); @Update suspend fun update(i:ShoppingItem); @Delete suspend fun delete(i:ShoppingItem); }
@Dao interface ReflectionDao { @Query("SELECT * FROM reflections WHERE month=:month") fun get(month:String):Flow<Reflection?>; @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(r:Reflection); }
@Database(entities=[Expense::class,Budget::class,ShoppingItem::class,Reflection::class],version=1,exportSchema=false)
abstract class KakeiboDatabase:RoomDatabase(){ abstract fun expenseDao():ExpenseDao; abstract fun budgetDao():BudgetDao; abstract fun shoppingDao():ShoppingDao; abstract fun reflectionDao():ReflectionDao
 companion object { @Volatile private var INSTANCE:KakeiboDatabase?=null; fun get(ctx:Context)=INSTANCE?:synchronized(this){INSTANCE?:Room.databaseBuilder(ctx.applicationContext,KakeiboDatabase::class.java,"kakeibo.db").build().also{INSTANCE=it}} }
}
