// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   FileCache.java

package xinhong_ucar.nc2.util.cache;

// Referenced classes of package xinhong_ucar.nc2.util.cache:
//			FileCacheable, FileFactory

public class FileCache {/*
    private class CleanupTask
            implements Runnable {

        final FileCache this$0;

        public void run() {
            cleanup(softLimit);
        }

        private CleanupTask() {
            this$0 = FileCache.this;
            super();
        }

    }

    class CacheElement {

        final List list = new LinkedList();
        final Object hashKey;
        final FileCache this$0;

        CacheFile addFile(FileCacheable ncfile) {
            CacheFile file = new CacheFile(ncfile);
            synchronized (this) {
                list.add(file);
            }
            if (FileCache.debug && files.get(ncfile) != null)
                FileCache.cacheLog.error((new StringBuilder()).append("files (2) already has ").append(hashKey).append(" ").append(name).toString());
            files.put(ncfile, file);
            return file;
        }

        public String toString() {
            return (new StringBuilder()).append(hashKey).append(" count=").append(list.size()).toString();
        }

        CacheElement(FileCacheable ncfile, Object hashKey) {
            this$0 = FileCache.this;
            super();
            this.hashKey = hashKey;
            CacheFile file = new CacheFile(ncfile);
            list.add(file);
            if (FileCache.debug && files.get(ncfile) != null)
                FileCache.cacheLog.error((new StringBuilder()).append("files already has ").append(hashKey).append(" ").append(name).toString());
            files.put(ncfile, file);
            if (FileCache.cacheLog.isDebugEnabled())
                FileCache.cacheLog.debug((new StringBuilder()).append("CacheElement add to cache ").append(hashKey).append(" ").append(name).toString());
        }
    }


    private static final Logger log = LoggerFactory.getLogger(xinhong_ucar / nc2 / util / cache / FileCache);
    private static final Logger cacheLog = LoggerFactory.getLogger("cacheLogger");
    private static ScheduledExecutorService exec;
    static boolean debug = false;
    static boolean debugPrint = false;
    static boolean debugCleanup = false;
    private String name;
    private final int softLimit;
    private final int minElements;
    private final int hardLimit;
    private final ConcurrentHashMap cache;
    private final ConcurrentHashMap files;
    private final AtomicBoolean hasScheduled;
    private final AtomicBoolean disabled;
    private final AtomicInteger cleanups;
    private final AtomicInteger hits;
    private final AtomicInteger miss;

    public static void shutdown() {
        if (exec != null)
            exec.shutdown();
        exec = null;
    }

    public FileCache(int minElementsInMemory, int maxElementsInMemory, int period) {
        this("", minElementsInMemory, maxElementsInMemory, -1, period);
    }

    public FileCache(int minElementsInMemory, int softLimit, int hardLimit, int period) {
        this("", minElementsInMemory, softLimit, hardLimit, period);
    }

    public FileCache(String name, int minElementsInMemory, int softLimit, int hardLimit, int period) {
        hasScheduled = new AtomicBoolean(false);
        disabled = new AtomicBoolean(false);
        cleanups = new AtomicInteger();
        hits = new AtomicInteger();
        miss = new AtomicInteger();
        this.name = name;
        minElements = minElementsInMemory;
        this.softLimit = softLimit;
        this.hardLimit = hardLimit;
        cache = new ConcurrentHashMap(2 * softLimit, 0.75F, 8);
        files = new ConcurrentHashMap(4 * softLimit, 0.75F, 8);
        if (period > 0) {
            if (exec == null)
                exec = Executors.newSingleThreadScheduledExecutor();
            exec.scheduleAtFixedRate(new CleanupTask(), period, period, TimeUnit.SECONDS);
            cacheLog.debug((new StringBuilder()).append("FileCache ").append(name).append(" cleanup every ").append(period).append(" secs").toString());
        }
    }

    public void disable() {
        disabled.set(true);
        clearCache(true);
    }

    public void enable() {
        disabled.set(false);
    }

    public FileCacheable acquire(FileFactory factory, String location, CancelTask cancelTask)
            throws IOException {
        return acquire(factory, location, location, -1, cancelTask, null);
    }

    public FileCacheable acquire(FileFactory factory, Object hashKey, String location, int buffer_size, CancelTask cancelTask, Object spiObject)
            throws IOException {
        if (null == hashKey)
            hashKey = location;
        FileCacheable ncfile = acquireCacheOnly(hashKey);
        if (ncfile != null) {
            hits.incrementAndGet();
            return ncfile;
        }
        miss.incrementAndGet();
        ncfile = factory.open(location, buffer_size, cancelTask, spiObject);
        if (cacheLog.isDebugEnabled())
            cacheLog.debug((new StringBuilder()).append("FileCache ").append(name).append(" acquire ").append(hashKey).append(" ").append(ncfile.getLocation()).toString());
        if (debugPrint)
            System.out.println((new StringBuilder()).append("  FileCache ").append(name).append(" acquire ").append(hashKey).append(" ").append(ncfile.getLocation()).toString());
        if (cancelTask != null && cancelTask.isCancel()) {
            if (ncfile != null)
                ncfile.close();
            return null;
        }
        if (disabled.get())
            return ncfile;
        CacheElement elem;
        synchronized (cache) {
            elem = (CacheElement) cache.get(hashKey);
            if (elem == null)
                cache.put(hashKey, new CacheElement(ncfile, hashKey));
        }
        if (elem != null)
            synchronized (elem) {
                elem.addFile(ncfile);
            }
        boolean needHard = false;
        boolean needSoft = false;
        synchronized (hasScheduled) {
            if (!hasScheduled.get()) {
                int count = files.size();
                if (count > hardLimit && hardLimit > 0) {
                    needHard = true;
                    hasScheduled.getAndSet(true);
                } else if (count > softLimit && exec != null) {
                    hasScheduled.getAndSet(true);
                    needSoft = true;
                }
            }
        }
        if (needHard) {
            if (debugCleanup)
                System.out.println((new StringBuilder()).append("CleanupTask due to hard limit time=").append((new Date()).getTime()).toString());
            cleanup(hardLimit);
        } else if (needSoft) {
            exec.schedule(new CleanupTask(), 100L, TimeUnit.MILLISECONDS);
            if (debugCleanup)
                System.out.println((new StringBuilder()).append("CleanupTask scheduled due to soft limit time=").append(new Date()).toString());
        }
        return ncfile;
    }

    private FileCacheable acquireCacheOnly(Object hashKey) {
        if (disabled.get())
            return null;
        CacheElement wantCacheElem = (CacheElement) cache.get(hashKey);
        if (wantCacheElem == null)
            return null;
        CacheElement.CacheFile want = null;
        synchronized (wantCacheElem) {
            Iterator i$ = wantCacheElem.list.iterator();
            do {
                if (!i$.hasNext())
                    break;
                CacheElement.CacheFile file = (CacheElement.CacheFile) i$.next();
                if (!file.isLocked.compareAndSet(false, true))
                    continue;
                want = file;
                break;
            } while (true);
        }
        if (want == null)
            return null;
        if (want.ncfile != null) {
            long lastModified = want.ncfile.getLastModified();
            boolean changed = lastModified != want.lastModified;
            if (cacheLog.isDebugEnabled() && changed)
                cacheLog.debug((new StringBuilder()).append("FileCache ").append(name).append(": acquire from cache ").append(hashKey).append(" ").append(want.ncfile.getLocation()).append(" was changed; discard").toString());
            if (changed) {
                want.remove();
                files.remove(want.ncfile);
                want.ncfile.setFileCache(null);
                try {
                    want.ncfile.close();
                } catch (IOException e) {
                    log.error((new StringBuilder()).append("close failed on ").append(want.ncfile.getLocation()).toString(), e);
                }
                want.ncfile = null;
            }
        }
        return want.ncfile;
    }

    public void remove(Object hashKey) {
        if (disabled.get())
            return;
        CacheElement wantCacheElem = (CacheElement) cache.get(hashKey);
        if (wantCacheElem == null)
            return;
        synchronized (wantCacheElem) {
            for (Iterator i$ = wantCacheElem.list.iterator(); i$.hasNext(); ) {
                CacheElement.CacheFile want = (CacheElement.CacheFile) i$.next();
                files.remove(want.ncfile);
                want.ncfile.setFileCache(null);
                try {
                    want.ncfile.close();
                    log.debug((new StringBuilder()).append("close ").append(want.ncfile.getLocation()).toString());
                } catch (IOException e) {
                    log.error((new StringBuilder()).append("close failed on ").append(want.ncfile.getLocation()).toString(), e);
                }
                want.ncfile = null;
            }

            wantCacheElem.list.clear();
        }
        cache.remove(hashKey);
    }

    public void release(FileCacheable ncfile)
            throws IOException {
        if (ncfile == null)
            return;
        if (disabled.get()) {
            ncfile.setFileCache(null);
            ncfile.close();
            return;
        }
        CacheElement.CacheFile file = (CacheElement.CacheFile) files.get(ncfile);
        if (file != null) {
            if (!file.isLocked.get()) {
                Exception e = new Exception("Stack trace");
                cacheLog.warn((new StringBuilder()).append("FileCache ").append(name).append(" release ").append(ncfile.getLocation()).append(" not locked; hash= ").append(ncfile.hashCode()).toString(), e);
            }
            file.lastAccessed = System.currentTimeMillis();
            file.countAccessed++;
            file.isLocked.set(false);
            if (cacheLog.isDebugEnabled())
                cacheLog.debug((new StringBuilder()).append("FileCache ").append(name).append(" release ").append(ncfile.getLocation()).append("; hash= ").append(ncfile.hashCode()).toString());
            if (debugPrint)
                System.out.println((new StringBuilder()).append("  FileCache ").append(name).append(" release ").append(ncfile.getLocation()).toString());
            return;
        } else {
            throw new IOException((new StringBuilder()).append("FileCache ").append(name).append(" release does not have file in cache = ").append(ncfile.getLocation()).toString());
        }
    }

    public String getInfo(FileCacheable ncfile)
            throws IOException {
        if (ncfile == null)
            return "";
        CacheElement.CacheFile file = (CacheElement.CacheFile) files.get(ncfile);
        if (file != null)
            return (new StringBuilder()).append("File is in cache= ").append(file).toString();
        else
            return "File not in cache";
    }

    Map getCache() {
        return cache;
    }

    public synchronized void clearCache(boolean force) {
        List deleteList = new ArrayList(2 * cache.size());
        if (force) {
            cache.clear();
            deleteList.addAll(files.values());
            files.clear();
        } else {
            Iterator iter = files.values().iterator();
            do {
                if (!iter.hasNext())
                    break;
                CacheElement.CacheFile file = (CacheElement.CacheFile) iter.next();
                if (file.isLocked.compareAndSet(false, true)) {
                    file.remove();
                    deleteList.add(file);
                    iter.remove();
                }
            } while (true);
            synchronized (cache) {
                for (Iterator i$ = cache.values().iterator(); i$.hasNext(); ) {
                    CacheElement elem = (CacheElement) i$.next();
                    synchronized (elem) {
                        if (elem.list.size() == 0)
                            cache.remove(elem.hashKey);
                    }
                }

            }
        }
        for (Iterator i$ = deleteList.iterator(); i$.hasNext(); ) {
            CacheElement.CacheFile file = (CacheElement.CacheFile) i$.next();
            if (force && file.isLocked.get())
                cacheLog.warn((new StringBuilder()).append("FileCache ").append(name).append(" force close locked file= ").append(file).toString());
            try {
                file.ncfile.setFileCache(null);
                file.ncfile.close();
                file.ncfile = null;
            } catch (IOException e) {
                log.error((new StringBuilder()).append("FileCache ").append(name).append(" close failed on ").append(file).toString());
            }
        }

        cacheLog.debug((new StringBuilder()).append("*FileCache ").append(name).append(" clearCache force= ").append(force).append(" deleted= ").append(deleteList.size()).append(" left=").append(files.size()).toString());
    }

    public void showCache(Formatter format) {
        ArrayList allFiles = new ArrayList(files.size());
        for (Iterator i$ = cache.values().iterator(); i$.hasNext(); ) {
            CacheElement elem = (CacheElement) i$.next();
            synchronized (elem) {
                allFiles.addAll(elem.list);
            }
        }

        Collections.sort(allFiles);
        format.format("FileCache %s (%d):%n", new Object[]{
                name, Integer.valueOf(allFiles.size())
        });
        format.format("isLocked  accesses lastAccess                   location %n", new Object[0]);
        CacheElement.CacheFile file;
        String loc;
        for (Iterator i$ = allFiles.iterator(); i$.hasNext(); format.format("%8s %9d %s %s %n", new Object[]{
                file.isLocked, Integer.valueOf(file.countAccessed), new Date(file.lastAccessed), loc
        })) {
            file = (CacheElement.CacheFile) i$.next();
            loc = file.ncfile == null ? "null" : file.ncfile.getLocation();
        }

    }

    public List showCache() {
        ArrayList allFiles = new ArrayList(files.size());
        for (Iterator i$ = cache.values().iterator(); i$.hasNext(); ) {
            CacheElement elem = (CacheElement) i$.next();
            synchronized (elem) {
                allFiles.addAll(elem.list);
            }
        }

        Collections.sort(allFiles);
        ArrayList result = new ArrayList(allFiles.size());
        CacheElement.CacheFile file;
        for (Iterator i$ = allFiles.iterator(); i$.hasNext(); result.add(file.toString()))
            file = (CacheElement.CacheFile) i$.next();

        return result;
    }

    public void showStats(Formatter format) {
        format.format("  hits= %d miss= %d nfiles= %d elems= %d\n", new Object[]{
                Integer.valueOf(hits.get()), Integer.valueOf(miss.get()), Integer.valueOf(files.size()), Integer.valueOf(cache.values().size())
        });
    }

    synchronized void cleanup(int maxElements) {
        if (disabled.get())
            return;
        int size = files.size();
        if (size <= minElements) {
            hasScheduled.set(false);
            return;
        }
        cacheLog.debug((new StringBuilder()).append(" FileCache ").append(name).append(" cleanup started at ").append(new Date()).append(" for cleanup maxElements=").append(maxElements).toString());
        if (debugCleanup)
            System.out.println((new StringBuilder()).append(" FileCache ").append(name).append("cleanup started at ").append(new Date()).append(" for cleanup maxElements=").append(maxElements).toString());
        cleanups.incrementAndGet();
        ArrayList allFiles = new ArrayList(size + 10);
        Iterator i$ = files.values().iterator();
        do {
            if (!i$.hasNext())
                break;
            CacheElement.CacheFile file = (CacheElement.CacheFile) i$.next();
            if (!file.isLocked.get())
                allFiles.add(file);
        } while (true);
        Collections.sort(allFiles);
        int need2delete = size - minElements;
        int minDelete = size - maxElements;
        ArrayList deleteList = new ArrayList(need2delete);
        int count = 0;
        Iterator iter = allFiles.iterator();
        do {
            if (!iter.hasNext() || count >= need2delete)
                break;
            CacheElement.CacheFile file = (CacheElement.CacheFile) iter.next();
            if (file.isLocked.compareAndSet(false, true)) {
                file.remove();
                deleteList.add(file);
                count++;
            }
        } while (true);
        if (count < minDelete) {
            cacheLog.warn((new StringBuilder()).append("FileCache ").append(name).append(" cleanup couldnt remove enough to keep under the maximum= ").append(maxElements).append(" due to locked files; currently at = ").append(size - count).toString());
            if (debugCleanup)
                System.out.println((new StringBuilder()).append("FileCache ").append(name).append("cleanup couldnt remove enough to keep under the maximum= ").append(maxElements).append(" due to locked files; currently at = ").append(size - count).toString());
        }
        synchronized (cache) {
            for (Iterator i$ = cache.values().iterator(); i$.hasNext(); ) {
                CacheElement elem = (CacheElement) i$.next();
                synchronized (elem) {
                    if (elem.list.size() == 0)
                        cache.remove(elem.hashKey);
                }
            }

        }
        long start = System.currentTimeMillis();
        for (Iterator i$ = deleteList.iterator(); i$.hasNext(); ) {
            CacheElement.CacheFile file = (CacheElement.CacheFile) i$.next();
            files.remove(file.ncfile);
            try {
                file.ncfile.setFileCache(null);
                file.ncfile.close();
                file.ncfile = null;
            } catch (IOException e) {
                log.error((new StringBuilder()).append("FileCache ").append(name).append(" close failed on ").append(file.getCacheName()).toString());
            }
        }

        long took = System.currentTimeMillis() - start;
        cacheLog.debug((new StringBuilder()).append(" FileCache ").append(name).append(" cleanup had= ").append(size).append(" removed= ").append(deleteList.size()).append(" took=").append(took).append(" msec").toString());
        if (debugCleanup)
            System.out.println((new StringBuilder()).append(" FileCache ").append(name).append("cleanup had= ").append(size).append(" removed= ").append(deleteList.size()).append(" took=").append(took).append(" msec").toString());
        hasScheduled.set(false);
        break MISSING_BLOCK_LABEL_838;
        Exception exception2;
        exception2;
        hasScheduled.set(false);
        throw exception2;
    }

*/
}