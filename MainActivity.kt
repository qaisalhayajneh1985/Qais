package com.kakeibo.personal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.*
import com.kakeibo.personal.data.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{KakeiboApp()}}}

@Composable fun KakeiboApp(vm:KakeiboVM=viewModel()){ MaterialTheme { CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl){ Surface{MainScreen(vm)} } } }

@Composable fun MainScreen(vm:KakeiboVM){
 var tab by remember{mutableIntStateOf(0)}
 Scaffold(bottomBar={NavigationBar{listOf("الرئيسية" to Icons.Default.Home,"المصروفات" to Icons.Default.ReceiptLong,"الميزانية" to Icons.Default.AccountBalanceWallet,"أغراض البيت" to Icons.Default.ShoppingCart,"التأمل" to Icons.Default.EditNote).forEachIndexed{i,p->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Icon(p.second,null)},label={Text(p.first)})}}}){pad->Box(Modifier.padding(pad).fillMaxSize()){when(tab){0->Dashboard(vm);1->Expenses(vm);2->BudgetScreen(vm);3->Shopping(vm);4->ReflectionScreen(vm)}}}}

fun money(x:Double)=NumberFormat.getNumberInstance(Locale("ar","JO")).format(x)+" د.أ"
@Composable fun CardStat(title:String,value:String,modifier:Modifier=Modifier){Card(modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(16.dp)){Text(title,style=MaterialTheme.typography.labelLarge);Text(value,style=MaterialTheme.typography.headlineSmall)}}}
@Composable fun Dashboard(vm:KakeiboVM){val es by vm.expenses.collectAsState(emptyList());val b by vm.budget.collectAsState();val total=es.sumOf{it.amount};val income=(b?.income?:0.0)+(b?.extraIncome?:0.0);val saving=b?.savingTarget?:0.0;val fixed=b?.fixedBudget?:0.0;LazyColumn(Modifier.padding(16.dp)){item{Text("لوحة كايبكو",style=MaterialTheme.typography.headlineMedium);Text("متابعة شهرية واعية للإنفاق",style=MaterialTheme.typography.bodyMedium);Spacer(Modifier.height(10.dp))};item{CardStat("إجمالي الدخل",money(income))};item{CardStat("هدف الادخار",money(saving))};item{CardStat("إجمالي المصروفات المتغيرة",money(total))};item{CardStat("الرصيد المتاح التقريبي",money(income-saving-fixed-total))};item{Spacer(Modifier.height(12.dp));Text("الفئات الأربع",style=MaterialTheme.typography.titleLarge)};listOf("الاحتياجات الأساسية","الكماليات والرغبات","الثقافة والترفيه","المصاريف الطارئة").forEach{c->item{val v=es.filter{it.category==c}.sumOf{it.amount};CardStat(c,money(v))}}}}

@Composable fun Expenses(vm:KakeiboVM){var show by remember{mutableStateOf(false)};val es by vm.expenses.collectAsState(emptyList());Box{LazyColumn(Modifier.padding(16.dp)){item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Text("سجل المصروفات",style=MaterialTheme.typography.headlineMedium);Button({show=true}){Icon(Icons.Default.Add,null);Text(" إضافة")}};Spacer(Modifier.height(8.dp))};items(es){e->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(e.detail);Text("${e.date} • ${e.category}",style=MaterialTheme.typography.bodySmall)}Text(money(e.amount));IconButton({vm.delete(e)}){Icon(Icons.Default.Delete,null)}}}}};if(show){AddExpenseDialog({show=false}){d->vm.add(d);show=false}}}}

@Composable fun AddExpenseDialog(onDismiss:()->Unit,onSave:(Expense)->Unit){var detail by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var cat by remember{mutableStateOf("الاحتياجات الأساسية")};var payment by remember{mutableStateOf("نقدي")};AlertDialog(onDismissRequest=onDismiss,title={Text("إضافة مصروف")},text={Column{OutlinedTextField(detail,{detail=it},label={Text("البند")});OutlinedTextField(amount,{amount=it},label={Text("المبلغ")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));Spacer(Modifier.height(8.dp));Text("الفئة");listOf("الاحتياجات الأساسية","الكماليات والرغبات","الثقافة والترفيه","المصاريف الطارئة").forEach{c->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(cat==c,{cat=c});Text(c)}};Text("طريقة الدفع: $payment");Row{Button({payment="نقدي"}){Text("نقدي")};Spacer(Modifier.width(8.dp));Button({payment="بطاقة"}){Text("بطاقة")}}}},confirmButton={Button(onClick={val a=amount.toDoubleOrNull()?:0.0;if(detail.isNotBlank()&&a>0)onSave(Expense(date=LocalDate.now().toString(),detail=detail,category=cat,payment=payment,amount=a))}){Text("حفظ")}},dismissButton={TextButton(onClick=onDismiss){Text("إلغاء")}})}

@Composable fun BudgetScreen(vm:KakeiboVM){val b by vm.budget.collectAsState();var income by remember(b){mutableStateOf((b?.income?:1400.0).toString())};var extra by remember(b){mutableStateOf((b?.extraIncome?:446.0).toString())};var saving by remember(b){mutableStateOf((b?.savingTarget?:100.0).toString())};Column(Modifier.padding(16.dp)){Text("الميزانية الشهرية",style=MaterialTheme.typography.headlineMedium);TextField(income,{income=it},label={Text("الدخل الأساسي")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth());TextField(extra,{extra=it},label={Text("مداخيل إضافية")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth());TextField(saving,{saving=it},label={Text("هدف الادخار")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(12.dp));Button({vm.saveBudget(income.toDoubleOrNull()?:0.0,extra.toDoubleOrNull()?:0.0,saving.toDoubleOrNull()?:0.0)}){Text("حفظ الميزانية")};Spacer(Modifier.height(20.dp));Text("المصاريف الثابتة في ملف Excel: 603 د.أ",style=MaterialTheme.typography.bodyLarge);Text("الفئات المتغيرة: 250 احتياجات، 120 كماليات، 50 ثقافة، 80 طوارئ",style=MaterialTheme.typography.bodyMedium)} }

@Composable fun Shopping(vm:KakeiboVM){val items by vm.shopping.collectAsState(emptyList());var text by remember{mutableStateOf("")};Column(Modifier.padding(16.dp)){Text("أغراض البيت",style=MaterialTheme.typography.headlineMedium);Row{TextField(text,{text=it},label={Text("غرض جديد")},modifier=Modifier.weight(1f));Button({if(text.isNotBlank()){vm.addShopping(text);text=""}}){Icon(Icons.Default.Add,null)}};LazyColumn{items(items){i->Row(Modifier.fillMaxWidth().padding(vertical=3.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(i.checked,{vm.toggle(i)});Text(i.name,Modifier.weight(1f));IconButton({vm.deleteShopping(i)}){Icon(Icons.Default.Delete,null)}}}}}}

@Composable fun ReflectionScreen(vm:KakeiboVM){val r by vm.reflection.collectAsState();var q1 by remember(r){mutableStateOf(r?.q1?:"")};var q2 by remember(r){mutableStateOf(r?.q2?:"")};var q3 by remember(r){mutableStateOf(r?.q3?:"")};var q4 by remember(r){mutableStateOf(r?.q4?:"")};Column(Modifier.padding(16.dp)){Text("تأمل وتقييم الشهر",style=MaterialTheme.typography.headlineMedium);listOf("كم المبلغ الذي نجحت في ادخاره هذا الشهر؟ وهل حققت الهدف؟" to q1,"ما الفئة التي استهلكت الجزء الأكبر من الميزانية المتغيرة؟" to q2,"ما المصاريف التي كان يمكن الاستغناء عنها أو تقليلها؟" to q3,"ما خطتك وتحسيناتك المالية للشهر القادم؟" to q4).forEachIndexed{i,p->{OutlinedTextField(p.second,{v->when(i){0->q1=v;1->q2=v;2->q3=v;3->q4=v}},label={Text(p.first)},modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),minLines=2)}};Button({vm.saveReflection(q1,q2,q3,q4)}){Text("حفظ التأمل")}}}

class KakeiboVM(app:android.app.Application):androidx.lifecycle.AndroidViewModel(app){private val db=KakeiboDatabase.get(app);val expenses=db.expenseDao().all();val shopping=db.shoppingDao().all();val budget=db.budgetDao().get("2026-09");val reflection=db.reflectionDao().get("2026-09");private val scope=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO);fun add(e:Expense)=scope.launch{db.expenseDao().insert(e)};fun delete(e:Expense)=scope.launch{db.expenseDao().delete(e)};fun saveBudget(i:Double,x:Double,s:Double)=scope.launch{db.budgetDao().save(Budget("2026-09",i,x,s))};fun addShopping(n:String)=scope.launch{db.shoppingDao().insert(ShoppingItem(name=n))};fun toggle(i:ShoppingItem)=scope.launch{db.shoppingDao().update(i.copy(checked=!i.checked))};fun deleteShopping(i:ShoppingItem)=scope.launch{db.shoppingDao().delete(i)};fun saveReflection(a:String,b:String,c:String,d:String)=scope.launch{db.reflectionDao().save(Reflection("2026-09",a,b,c,d))}}
