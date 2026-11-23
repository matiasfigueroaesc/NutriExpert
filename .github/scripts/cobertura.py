import sys, glob, xml.etree.ElementTree as ET
f = glob.glob('app/build/reports/jacoco/**/*.xml', recursive=True)[0]
r = ET.parse(f).getroot()
def cov(e):
    for c in e.findall('counter'):
        if c.get('type') == 'LINE':
            return int(c.get('covered')), int(c.get('missed'))
    return 0, 0
a, b = cov(r); print("COBERTURA TOTAL %.1f%% (%d/%d)" % (100*a/(a+b), a, a+b))
tot = {}
for p in r.findall('package'):
    n = p.get('name').split('/')[-1]
    if n not in ('viewmodel', 'repository'): continue
    for c in p.findall('class'):
        x, y = cov(c); s = tot.setdefault(n, [0, 0]); s[0] += x; s[1] += y
        base = c.get('name').split('/')[-1]
        if '$' not in base and x + y: print("COB %s %.1f%% (%d/%d)" % (base, 100*x/(x+y), x, x+y))
for k, (x, y) in tot.items(): print("COBERTURA %s %.1f%% (%d/%d)" % (k, 100*x/(x+y), x, x+y))
t = glob.glob('app/build/test-results/testDebugUnitTest/*.xml')
n = sum(int(ET.parse(x).getroot().get('tests')) for x in t); fl = sum(int(ET.parse(x).getroot().get('failures')) for x in t)
print("TESTS %d fallidos %d" % (n, fl))
