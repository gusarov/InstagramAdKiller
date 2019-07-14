package ru.xkip.noad.instagram

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Modifier
// import kotlin.reflect.KClass


class Xposed : IXposedHookLoadPackage {
	companion object {
		val tag = "NOAD"
		val appid = "com.instagram.android"
		var context: Context? = null
		val abc = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

		val markedViews = mutableSetOf<View>()
		// val hookedClasses = mutableSetOf<Class<*>>()
/*
		var OnLayoutChangeListener: View.OnLayoutChangeListener =
			View.OnLayoutChangeListener { vw, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
				HandleView2(
					vw,
					false
				)
			};
*/
		// FIXED NAME
		var qpsClass: Class<*>? = null

		// DYNAMIC NAME
		var postFilterClass: Class<*>? = null
		val medias = mutableSetOf<Class<*>>()
		var bestMedia: Class<*>? = null
		var sponsoredFieldName: String? = null
		var sponsoredFieldType: Class<*>? = null
		var dynamicDataSaved = false
		var curVer = ""

		fun saveDynamicData() {
			Settings.setSetting("postFilterClass", postFilterClass?.name as String)
			Settings.setSetting("bestMedia", bestMedia?.name as String)
			Settings.setSetting("sponsoredFieldName", sponsoredFieldName as String)
			Settings.setSetting("sponsoredFieldType", sponsoredFieldType?.name as String)
			Settings.setSetting("postFilterClass", postFilterClass?.name as String)
			Settings.setSetting("ver", curVer)
			Log.i(tag, "Dynamic Data Saved - $curVer")

			dynamicDataSaved = true
		}

		fun loadDynamicData(cl: ClassLoader) {
			dynamicDataSaved = true // no need to save again

			postFilterClass = XposedHelpers.findClass(Settings.getSetting("postFilterClass"), cl)
			bestMedia = XposedHelpers.findClass(Settings.getSetting("bestMedia"), cl)
			sponsoredFieldName = Settings.getSetting("sponsoredFieldName")
			sponsoredFieldType = XposedHelpers.findClass(Settings.getSetting("sponsoredFieldType"), cl)
			postFilterClass = XposedHelpers.findClass(Settings.getSetting("postFilterClass"), cl)

			Log.i(tag, "Loaded: postFilterClass=$postFilterClass")
			Log.i(tag, "Loaded: bestMedia=$bestMedia")
			Log.i(tag, "Loaded: sponsoredFieldName=$sponsoredFieldName")
			Log.i(tag, "Loaded: sponsoredFieldType=$sponsoredFieldType")
			Log.i(tag, "Loaded: postFilterClass=$postFilterClass")
		}

		/*
		fun HandleView2(view: View, beenDelayed: Boolean) {
			if (beenDelayed) {
				HandleView2Real(view)
			} else {
				Handler().postDelayed({ HandleView2Real(view)}, 1)
			}
		}

		val tagKey = 7158192

		*/

		fun handleView2Real(view: View, hide: Boolean, fade: Boolean = false) {
			if (hide) {
				if (fade) {
					view.alpha = 0.1f
				} else {
					val params = view.layoutParams
					params.height = 1
					view.layoutParams = params
					view.visibility = View.GONE
				}
			} else {
				val params = view.layoutParams
				params.height = ViewGroup.LayoutParams.WRAP_CONTENT
				view.layoutParams = params
				view.visibility = View.VISIBLE
				view.alpha = 1f
			}
		}

			// OLD
		fun HandleView(view: View, delayed: Boolean){
			Handler().postDelayed({
				// var parent = view.parent;
				// var i = 0
				// while (parent != null)

				// Log.i(tag, "+++ $parent")
				// parent = parent.parent
				// i++;
				//if (i == 5)

				var q = 0
				val r: Int = q % 3
				q++
				Log.i(tag, "$q % 3 = ${r}")

				var parent = view.parent;
				var i = 0
				while (parent != null) {
					Log.i(tag, "+++ $parent")
					if (i == 1) {
						if (parent is ViewGroup) {
							Log.i(tag, "LIST KIDS")
							for (i in 0..parent.childCount){
								val c = parent.getChildAt(i)
								Log.i(tag, "v[$i]=$c")
								when (i) {
									0 -> {
										parent.alpha = 0.1f
										parent.setBackgroundColor(Color.argb(128, 255, 0, 0))
									}
									1 -> {
										parent.alpha = 0.2f
										parent.setBackgroundColor(Color.argb(128, 0, 255, 0))
									}
									2 -> {
										parent.alpha = 0.3f
										parent.setBackgroundColor(Color.argb(128, 0, 0, 255))
									}
									3 -> {
										parent.alpha = 0.4f
										parent.setBackgroundColor(Color.argb(128, 255, 255, 0))
									}
									4 -> {
										parent.alpha = 0.5f
										parent.setBackgroundColor(Color.argb(128, 255, 0, 255))
									}
									5 -> {
										parent.alpha = 0.6f
										parent.setBackgroundColor(Color.argb(128, 0, 255, 255))
									}
									else -> {
										parent.alpha = 1f
										parent.setBackgroundColor(Color.argb(255, 255, 255, 255))
									}
								}
							}

						}
						/*
						if (parent is View) {
							when (r) {
								0 -> {
									parent.alpha = 0.1f
									parent.setBackgroundColor(Color.argb(128, 255, 0, 0))
								}
								1 -> {
									parent.alpha = 0.5f
									parent.setBackgroundColor(Color.argb(128, 0, 255, 0))
								}
								2 -> {
									parent.alpha = 0.8f
									parent.setBackgroundColor(Color.argb(128, 0, 0, 255))
								}
							}
						}
						*/
						break
					}
					parent = parent.parent
					i++;
				}

				Log.i(tag, "done")
			}, 100)
		}
	}

	override fun handleLoadPackage(lpparam: LoadPackageParam?) {
		try {
			if (lpparam == null) {
				return
			}

			// Log.i(tag, "Loading " + lpparam?.packageName)

			if (lpparam.packageName.contains("ru.xkip.noad.instagram")) {
				XposedHelpers.findAndHookMethod(
					"ru.xkip.noad.instagram.XChecker",
					lpparam.classLoader,
					"isEnabled",
					XC_MethodReplacement.returnConstant(java.lang.Boolean.TRUE)
				)
			}

			if (lpparam.packageName != appid) {
				return
			}

			if (context == null) {
				val activityThread = XposedHelpers.callStaticMethod(
					XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread"
				)
				context = XposedHelpers.callMethod(activityThread, "getSystemContext") as Context
			}
			val ctx = context as Context;
			val version = ctx.packageManager.getPackageInfo(lpparam.packageName, 0).versionName
			val moduleVersion = ctx.packageManager.getPackageInfo("ru.xkip.noad.instagram", 0).versionName

			Log.i(tag, "Instagram: ${lpparam.packageName} $version loaded with module version $moduleVersion")

			resolveNamedClass(lpparam.classLoader)

			curVer = "$version + $moduleVersion"
			var lastVer = Settings.getSetting("ver")
			if (lastVer != curVer) {
				Log.i(tag, "New version detected: $lastVer => $curVer. Performing class search...")
				// 2 pass required - first detects model classes
				// all dynamic class name fields are empty - will be filled on the fly
				bf(lpparam)
				bf(lpparam)
			} else {
				Log.i(tag, "This version already been analyzed, loading class names...")
				// load dynamic class names & resolve
				loadDynamicData(lpparam.classLoader)
				hookSponsoredPost(postFilterClass as Class<*>, true)
			}
		} catch (e: Throwable) {
			Log.e(tag, "err", e)
		}
	}

	fun resolveNamedClass(classLoader: ClassLoader) {
		if (qpsClass == null) {
			qpsClass =
				XposedHelpers.findClass("com.instagram.quickpromotion.intf.QuickPromotionSurface", classLoader)
			if (qpsClass != null) {
				Log.i(tag, "QuickPromotionSurface - success: $qpsClass")
			} else {
				Log.i(tag, "QuickPromotionSurface - failed)")
				return
			}
		}
	}

	fun bf(lpparam: LoadPackageParam){
		resolveNamedClass(lpparam.classLoader)

		var total = 0
		var failInRow = 0

		//findAndHookAds("X.1IY", lpparam)

		try {
		m@	for (a in 0..(abc.length-1))
			for (b in 0..(abc.length-1))
			for (c in 0..(abc.length-1)) {
				var ok = findAndHookAds("X." + abc[a] + abc[b] + abc[c], lpparam)
				total += if (ok) 1 else 0
				failInRow += if (!ok) 1 else 0
				if (ok) {
					failInRow = 0
				}
				if (failInRow > 100) {
					break@m
				}
			}
		} catch (ex: Throwable) {
			Log.e(tag, "Brute Force Ex", ex)
		}

		Log.i(tag, "Brute Force Done: $total")
	}

	fun serialize(obj: Any): String {
		var s = "["
		for (f in obj::class.java.fields) {
			// if (f.type == String.javaClass)

			val v = f.get(obj)
			s += "${f.name}=$v; "

		}
		s += "]"
		return s
	}

	fun hookSponsoredPost(clazz: Class<*>, required: Boolean) {
		try {
			// if (!hookedClasses.add(clazz)) {
				// return
			// }
			val methods = XposedHelpers.findMethodsByExactParameters(
				clazz,
				View::class.java,
				Int::class.javaPrimitiveType,
				View::class.java,
				ViewGroup::class.java,
				Any::class.java,
				Any::class.java
			)
			if(methods.isNotEmpty()) {
				// Log.i(tag, "hookSponsoredPost - ${clazz.name} - ${methods.size} methods")
				XposedBridge.hookMethod(methods[0], object : XC_MethodHook() {
					@Throws(Throwable::class)
					override fun afterHookedMethod(param: MethodHookParam?) {
						super.afterHookedMethod(param)
						val objects = param!!.args

						for (obj in objects) {
							try {
								if ( if (bestMedia != null) bestMedia == obj::class.java else medias.contains(obj::class.java)) {
									Log.w(tag, "Method called: ${clazz.name} ${methods[0].name} (${obj::class.java.name} $obj)")
									val view = param.args[1] as View?

									if (sponsoredFieldName == null && view != null) {
										for (f in obj::class.java.fields) {
											var v = f.get(obj)
											// analyze feed.fields
											if (v != null) {
												var meths =
													v::class.java.methods.count { m -> !m.toString().contains("java.lang.")} // 0
												var strs =
													v::class.java.fields.count { f -> f.type == String::class.java } // 7
												var bools =
													obj::class.java.fields.count { f -> f.type == Boolean::class.java } // 9
												// var ints = obj::class.java.fields.count { f -> f.type.isPrimitive} // 1
												if (strs >= 6 && bools >= 8 && meths == 0) { // this is how advert info data object looks like
													sponsoredFieldName = f.name
													sponsoredFieldType = f.type
													Log.w(tag, "Sponsored Hook Field Name = $sponsoredFieldName: ${sponsoredFieldType?.name}")
													break
													/*
													for (m in v::class.java.methods) {
														Log.i(tag, ">>> Method $m")
													}
													*/
												}
											}
										}
									}

									var hide = false
									if (sponsoredFieldName != null && view != null) {
										val adCheck = XposedHelpers.getObjectField(obj, sponsoredFieldName)
										if (adCheck != null) {
											Log.w(
												tag,
												"Sponsored ad detected ${obj::class.java}.$sponsoredFieldName = ${serialize(adCheck)} adCheck=$adCheck view=$view"
											)
											hide = true
											// This is the guy! Next time subscribe only here
											postFilterClass = clazz
											bestMedia = obj::class.java
											if (!dynamicDataSaved) {
												saveDynamicData()
											}
										} else {

										}

										/*
										if (!hide) {
											Handler().postDelayed({
												// try again a bit later
												var isAd = XposedHelpers.getObjectField(obj, sponsoredFieldName) != null
												Log.i(tag, "Second attempt: isAd = $isAd")
												if (isAd) {
													// but only fade, because view might belong to the other post by now!
													handleView2Real(view, hide = true, fade = true)
												}
											}, 2000)
										}
										*/
									}

									if (view != null) {
										handleView2Real(view, hide)
									}
/*
									if (adCheck != null) {
										if (view.visibility != View.GONE) {
											val params = view.layoutParams
											params.height = 1
											view.layoutParams = params
											view.visibility = View.GONE
										}
									} else {
										try {
											val paidCheck = XposedHelpers.getObjectField(obj, PAID_HOOK)
											if (paidCheck != null) {
												if (view.visibility != View.GONE) {

													if (Helper.getSettings("SponsorPost")) {
														val params = view.layoutParams
														params.height = 1
														view.layoutParams = params
														// view.visibility = View.GONE
														view.alpha = 0.2f
													}

												}
											} else {
												val params = view.layoutParams
												params.height = ViewGroup.LayoutParams.WRAP_CONTENT
												view.layoutParams = params
												view.visibility = View.VISIBLE
												view.alpha = 1f
											}
										} catch (t: Throwable) {
											val params = view.layoutParams
											params.height = ViewGroup.LayoutParams.WRAP_CONTENT
											view.layoutParams = params
											view.visibility = View.VISIBLE
											view.alpha = 1f
										}
									}
									*/
								}
							} catch (e: NullPointerException) {
								try {
									val view = param.args[1] as View
									val params = view.layoutParams
									params.height = ViewGroup.LayoutParams.WRAP_CONTENT
									view.layoutParams = params
									view.visibility = View.VISIBLE
									view.alpha = 1f
								} catch (t2: Throwable) {
								}

							}

						}
					}
				})
			}
/*
			XposedHelpers.findAndHookMethod(
				sponsoredClass,
				methods[0].name,
				Int::class.javaPrimitiveType,
				View::class.java,
				ViewGroup::class.java,
				Any::class.java,
				Any::class.java,
				object : XC_MethodHook() {
					@Throws(Throwable::class)
					override fun afterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
						super.afterHookedMethod(param)
						val objects = param!!.args

						for (obj in objects) {
							try {
								if (obj.javaClass.name == MEDIA_CLASS_NAME) {
									val view = param.args[1] as View

									val adCheck = XposedHelpers.getObjectField(obj, SPONSORED_HOOK)
									if (adCheck != null) {
										if (view.visibility != View.GONE) {
											val params = view.layoutParams
											params.height = 1
											view.layoutParams = params
											view.visibility = View.GONE
										}
									} else {
										try {
											val paidCheck = XposedHelpers.getObjectField(obj, PAID_HOOK)
											if (paidCheck != null) {
												if (view.visibility != View.GONE) {

													if (Helper.getSettings("SponsorPost")) {
														val params = view.layoutParams
														params.height = 1
														view.layoutParams = params
														view.visibility = View.GONE
													}

												}
											} else {
												val params = view.layoutParams
												params.height = ViewGroup.LayoutParams.WRAP_CONTENT
												view.layoutParams = params
												view.visibility = View.VISIBLE
											}
										} catch (t: Throwable) {
											val params = view.layoutParams
											params.height = ViewGroup.LayoutParams.WRAP_CONTENT
											view.layoutParams = params
											view.visibility = View.VISIBLE
										}

									}
								}
							} catch (e: NullPointerException) {
								try {
									val view = param.args[1] as View
									val params = view.layoutParams
									params.height = ViewGroup.LayoutParams.WRAP_CONTENT
									view.layoutParams = params
									view.visibility = View.VISIBLE
								} catch (t2: Throwable) {
								}

							}

						}
					}
				})
				*/
		} catch (ex: Throwable) {
			if (required) {
				Log.e(tag, "hooking error", ex)
			}
		}

	}

	// "com.fasterxml.jackson.core.JsonParser"

	fun findAndHookAds(name: String, lpparam: LoadPackageParam): Boolean {
		val classObj: Class<*>

		try {
			classObj = XposedHelpers.findClass(name, lpparam.classLoader)
			try{
				for (m in classObj.declaredMethods) {
					if (m.parameterTypes.size == 2 && Modifier.isPublic(m.modifiers)) {
						if (m.parameterTypes[0].name == "com.fasterxml.jackson.core.JsonParser") {
							// Log.i(tag,"MEDIA almost found: $classObj $m ${m.parameterTypes[1].name} - ${m.parameterTypes[1]} ${m.parameterTypes[1] == Boolean::class.java}")
							if (m.parameterTypes[1].name == "boolean") {
								if (m.returnType == classObj && medias.add(classObj)) {
									Log.i(tag,"MEDIA found: $classObj $m")
								}
							}
						}
					}
				}
				/*
				val meth = XposedHelpers.findMethodsByExactParameters(
					classObj,
					JsonParser
					Int::class.javaPrimitiveType,
					View::class.java,
					ViewGroup::class.java,
					Any::class.java,
					Any::class.java
				)
				if (meth.isNotEmpty()) {
					Log.i(tag, "SUCCESS: $classObj - ${meth[0]}")
				}
				*/
			} catch (ex: Throwable) {

			}
			hookSponsoredPost(classObj, false)

			/*
			try{
				val meth = XposedHelpers.findMethodsByExactParameters(
					classObj,
					View::class.java,
					Int::class.javaPrimitiveType,
					View::class.java,
					ViewGroup::class.java,
					Any::class.java,
					Any::class.java
				)
				if (meth.isNotEmpty()) {
					Log.i(tag, "SUCCESS: $classObj - ${meth[0]}")
				}
			}catch (ex: Throwable) {

			}
			*/
			/*
			try {
				//classObj.`package`.javaClass
				// Log.i(tag, "Class $name is ${if (classObj != null) "found" else "not found" } $classObj")
				// return classObj != null

				// find method by parameters
				m@ for (m in classObj.declaredMethods) {
					for (p in m.parameterTypes) {
						if (p == qpsClass) {
							Log.i(tag, "Found! $classObj $m ${m.name}")
							var ret = m.returnType
							if (ret.declaredMethods.size == 2) {
								Log.i(tag, ">> 1: class ${ret.name} have 2 methods")

								for (m in ret.declaredMethods) {
									Log.i(tag, ">>> Method ${m.name}")
									for (p in m.parameterTypes) {
										Log.i(tag, ">>>> Parameter ${p.name} === $p")
									}
								}

								val emp = ret.declaredMethods.find { m -> m.parameterTypes.isEmpty() }
								val str = ret.declaredMethods.find { m -> m != emp }
								var add = patched.add(classObj)
								Log.i(
									tag,
									"emp ($emp) str ($str) str != null ${str != null} emp != null ${emp != null} add=$add"
								)

								if (str != null && emp != null && add) {
									/ *
									Log.i(tag, "PATCHING...")
									val sub = XC_MethodReplacement.returnConstant(
										XposedHelpers.callStaticMethod(
											ret,
											str.name,
											"no"
										)
									)
									XposedBridge.hookMethod(m, sub)
									Log.i(tag, "PATCHED ${classObj.name}")
									* /
								}

								if (str != null && patched.add(ret)) {
									/*
									Log.i(tag, "PATCHING RET...")
									val sub = XC_MethodReplacement.returnConstant(
										XposedHelpers.callStaticMethod(
											ret,
											str.name,
											"no"
										)
									)
									XposedBridge.hookMethod(emp, sub)
									Log.i(tag, "PATCHED RET ${ret.name}")
									*/
								}
							}

							/ *
							for (m2 in ret.declaredMethods) {
								if (m2.parameterTypes.isEmpty() && m2.returnType == ret) {
									Log.i(tag, ">> GOOD! (${ret.name}) $ret $m2")
									val mstr = ret.declaredMethods.find { x -> x.parameterTypes.size == 1 && x.parameterTypes[0] == String.javaClass }
									if(mstr != null ) {
										Log.i(tag, ">> HOOK!")
										XposedHelpers.findAndHookMethod(
											classObj,
											m.name,
											XC_MethodReplacement.returnConstant(
												XposedHelpers.callStaticMethod(
													classObj,
													mstr.name,
													"no"
												)
											)
										)
										Log.i(tag, ">> HOOKED!")
									}
								}
							}
							* /
							// break@m
						}
					}
				}
			} catch (ex: Throwable) {
				Log.e(tag, "Error", ex)
			}
			*/
			// var res = XposedHelpers.findMethodsByExactParameters(classObj, null,null, null, qpsClass, null, null)
			/*
			if (!res.isNullOrEmpty() ) {
				Log.i(tag, "Brute - success: ${res[0]} ${res[0].name}")
			} else {
				Log.i(tag, "res: $res")
			}
			*/
			return true
		} catch (ex: Throwable) {
			// Log.e(tag, "load class", ex)
			return false
		}

	}

	private fun hookViews(lpparam: LoadPackageParam) {
		val mViewGroup = XposedHelpers.findClass("android.view.ViewGroup", lpparam.classLoader)
		XposedBridge.hookAllMethods(mViewGroup, "addView", object : XC_MethodHook() {
			@Throws(Throwable::class)
			override fun afterHookedMethod(param: MethodHookParam?) {

				checkAndHideAdViewCards(param, lpparam)
			}
		})
	}

	private fun checkAndHideAdViewCards(param: XC_MethodHook.MethodHookParam?, lpparam: LoadPackageParam) {
		try {
			val view = param!!.args[0] as View
			Log.i(tag, "After addView " + view + " " + view::class.simpleName)

			if (view.toString().contains("row_feed_profile_header_container")) {

				//view.removeOnLayoutChangeListener(OnLayoutChangeListener)
				//view.addOnLayoutChangeListener(OnLayoutChangeListener)

				// handle row on first creation
				val handler = Handler()
				handler.postDelayed({ HandleView(view, true) }, 1)

				// view.setBackgroundColor(Color.argb(128, 0, 255, 0))

			}
			if (view.toString().contains("app:id/username")) {
				//view.setBackgroundColor(Color.argb(128, 255, 0, 0))
			}
			if (view.toString().contains("app:id/subtitle")) {
				// view.setBackgroundColor(Color.argb(128, 255, 255, 0))
			}
			if (view.toString().contains("app:id/row_feed_photo_profile_name")) {
				// view.setBackgroundColor(Color.argb(128, 0, 255, 0))
			}

			if (view.toString().contains("app:id/row_feed_photo_subtitle")) {
				// view.setBackgroundColor(Color.argb(128, 255, 0, 0))


			}
			if (view.toString().contains("app:id/row_feed_photo_profile_metalabel")) {
				//view.setBackgroundColor(Color.argb(128, 255, 0, 0))
			}
/*
			if (view is TextView) {
				view.text
				Log.i(tag, ">>> " + view.text.toString())
			}
*/
			if (view.toString().contains("branded")) {
				// view.setBackgroundColor(Color.argb(128, 0, 255, 255))
			}



			/*
			if (view is ViewGroup) {
				if (view.toString().contains("app:id/row")) {
					// Log.i(tag, "row " + view);
					// subscribe once to handle reusable rows
					view.removeOnLayoutChangeListener(OnLayoutChangeListener)
					view.addOnLayoutChangeListener(OnLayoutChangeListener)

					// handle row on first creation
					val handler = Handler()
					handler.postDelayed({ HandleView(view, true) }, 1)
				}
			}
			*/
		} catch (ignored: Throwable) {
			Log.e(tag, "err", ignored)
		}

	}

}