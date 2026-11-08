# Search benchmark measurements

Runtime: 21.0.11+9-LTS-211; Java HotSpot(TM) 64-Bit Server VM. OS: Windows 11 10.0 amd64.
JVM arguments: [-XX:ActiveProcessorCount=1, -Xmx384m]
Each cell is microseconds per call: elapsed nanoTime / 1000 / 50.
Each method/case has 200 untimed warm-up calls, then 3 measured batches of 50 calls. JIT remains enabled; no explicit GC. Allocation and preprocessing are included.
All three outputs are checked against startsWith before warm-up. Volatile sink retains results.
Random: java.util.Random(seed=20261107+n), alphabet abcd; pattern is text[n/3:n/3+m].
Repetitive: text=a^n, pattern=a^m. Near-miss: text=a^n, pattern=a^(m-1)b.

|Case|n|m|Seed|Algorithm|Run 1 (us/call)|Run 2|Run 3|Median|Correct outputs checked|
|---|---:|---:|---:|---|---:|---:|---:|---:|---|
|random|750|187|20261857|naive|25.668|28.718|26.438|26.438|yes (1 starts)|
|random|750|187|20261857|kmp|35.810|40.416|38.470|38.470|yes (1 starts)|
|random|750|187|20261857|rk|139.826|116.622|119.564|119.564|yes (1 starts)|
|random|1500|375|20262607|naive|60.928|5.434|4.558|5.434|yes (1 starts)|
|random|1500|375|20262607|kmp|9.188|7.980|8.878|8.878|yes (1 starts)|
|random|1500|375|20262607|rk|164.438|165.454|161.936|164.438|yes (1 starts)|
|random|3000|750|20264107|naive|9.676|9.076|9.000|9.076|yes (1 starts)|
|random|3000|750|20264107|kmp|17.068|16.072|16.100|16.100|yes (1 starts)|
|random|3000|750|20264107|rk|41.062|40.892|40.946|40.946|yes (1 starts)|
|repetitive|750|187|n/a|naive|209.164|210.836|217.774|210.836|yes (564 starts)|
|repetitive|750|187|n/a|kmp|14.014|13.676|13.602|13.676|yes (564 starts)|
|repetitive|750|187|n/a|rk|476.882|479.068|497.730|479.068|yes (564 starts)|
|repetitive|1500|375|n/a|naive|816.136|811.800|846.222|816.136|yes (1126 starts)|
|repetitive|1500|375|n/a|kmp|25.230|25.980|26.638|25.980|yes (1126 starts)|
|repetitive|1500|375|n/a|rk|1876.972|1881.380|1896.174|1881.380|yes (1126 starts)|
|repetitive|3000|750|n/a|naive|5045.612|4928.498|4836.442|4928.498|yes (2251 starts)|
|repetitive|3000|750|n/a|kmp|58.326|76.122|80.818|76.122|yes (2251 starts)|
|repetitive|3000|750|n/a|rk|9628.366|9749.698|9847.134|9749.698|yes (2251 starts)|
|near-miss|750|187|n/a|naive|315.252|309.316|300.098|309.316|yes (0 starts)|
|near-miss|750|187|n/a|kmp|11.446|12.004|7.030|11.446|yes (0 starts)|
|near-miss|750|187|n/a|rk|10.522|10.078|9.344|10.078|yes (0 starts)|
|near-miss|1500|375|n/a|naive|1284.096|1233.120|1322.898|1284.096|yes (0 starts)|
|near-miss|1500|375|n/a|kmp|14.450|17.754|23.250|17.754|yes (0 starts)|
|near-miss|1500|375|n/a|rk|12.930|13.838|17.148|13.838|yes (0 starts)|
|near-miss|3000|750|n/a|naive|4925.890|4931.588|4705.400|4925.890|yes (0 starts)|
|near-miss|3000|750|n/a|kmp|45.244|44.450|45.984|45.244|yes (0 starts)|
|near-miss|3000|750|n/a|rk|33.020|33.954|35.232|33.954|yes (0 starts)|
