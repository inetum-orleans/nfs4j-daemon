# nfs4j-daemon

Pure Java NFS v3/v4.1 server backed by [dCache nfs4j](https://github.com/dCache/nfs4j).

This project has been designed as an alternative to [winnfsd](https://github.com/winnfsd/winnfsd).

*NFS v3 is available, but not untested and unsupported. You should use NFS v4.1 only for now.*

## TODO:

- MacOS support.
- Vagrant plugin.

## Quickstart

- Download latest binaries from [Github Releases](https://github.com/Toilal/nfs4j-daemon/releases).

- Run `nfs4j-daemon`. With default options, it will publish the current working directory through NFS.

```
java -jar nfs4j-daemon.jar
```

- Windows users may use the `.exe` wrapper.

```
nfs4j-daemon.exe
```

- Mount the share on any OS supporting NFS.

```
mkdir /mnt/nfs4j
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

## Options

All options are available through Command Line and Configuration File.

### Command Line

```
java -jar nfs4j-daemon.jar --help
```

```
Usage: <main class> [-hu] [-c=<config>] [-e=<exports>] [-p=<port>] [<shares>...]
      [<shares>...]         Directories to share
  -c, --config=<config>     Path to configuration file
  -e, --exports=<exports>   Path to exports file (nsf4j advanced configuration)
  -h, --help                Display this help message
  -p, --port=<port>         Port to use
  -u, --udp                 Use UDP instead of TCP
```

### Configuration File

Configuration file is loaded from *nfs4j.yml* in working directory by default.

You can set a custom filepath to this configuration file with `-c, --config=<config>` command line option.
```
port: 2048
udp: false
shares:
  - 'C:\Users\Toilal\projects\planireza'
  - 'C:\Users\Toilal\projects\docker-devbox'
  - 'D:\'
```

*Make sure you are using single quote on shares definition strings in yaml configuration file to avoid issues 
with backslashes.*

## Shares configuration

- If no share is configured, the current working directory is published under the root alias ```/```.

```
// Server side
java -jar nfs4j-daemon.jar
// Client side
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

- If a single share is configured, it's published under the root alias ```/```.

```
// Server side
java -jar nfs4j-daemon.jar C:\my\folder
// Client side
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

- If many shares are configured, they will be aliased automatically based on their local path.

```
// Server side
java -jar nfs4j-daemon.jar C:\my\folder D:\another\folder
// Client side
mount -t nfs 192.168.1.1:/C/my/folder /mnt/nfs4j-1
mount -t nfs 192.168.1.1:/D/another/folder /mnt/nfs4j-2
```

- Alias can be defined manually by adding its value after the local path of the share, using 
```:``` as separator.

```
// Server side
java -jar nfs4j-daemon.jar C:\my\folder:/folder1 D:\another\folder:/folder2
// Client side
mount -t nfs 192.168.1.1:/folder1 /mnt/nfs4j-1
mount -t nfs 192.168.1.1:/folder2 /mnt/nfs4j-2
```

- Or using the configuration file, with string syntax.

```
shares:
  - 'C:\my\folder:/folder1'
  - 'D:\another\folder:/folder2'
```

- Or using the configuration file, with object syntax.

```
shares:
  - path: 'C:\my\folder'
    alias: '/folder1'
  - path: 'D:\another\folder'
    alias: '/folder2'
```

