# nfs4j-daemon

Pure Java NFS v3/v4.1 server backed by [dCache nfs4j](https://github.com/dCache/nfs4j).

This project has been designed as an alternative to [winnfsd](https://github.com/winnfsd/winnfsd).

*NFS v3 is available, but not untested and unsupported. You should use NFS v4.1 only for now.*

## TODO:

- MacOS support.
- Vagrant plugin.

## Quickstart

- Download latest binaries from [Github Releases](https://github.com/gfi-centre-ouest/nfs4j-daemon/releases).

- Run `nfs4j-daemon`. With default options, it will publish the current working directory through NFS.

```bash
java -jar nfs4j-daemon.jar
```

- Windows users may use the `.exe` wrapper.

```bash
nfs4j-daemon.exe
```

- Mount the share on any OS supporting NFS.

```bash
mkdir /mnt/nfs4j
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

## Options

All options are available through Command Line and Configuration File.

### Command Line

```bash
java -jar nfs4j-daemon.jar --help
```

```bash
Usage: <main class> [-h] [--portmap-disabled] [--udp] [-c=<config>]
                    [-e=<exports>] [-g=<gid>] [-m=<mask>] [-p=<port>]
                    [-u=<uid>] [<shares>...]
      [<shares>...]         Directories to share
  -c, --config=<config>     Path to configuration file
  -u, --uid=<uid>           Default user id to use for exported files
  -g, --gid=<gid>           Default group id to use for exported files
  -m, --mask=<mask>         Default mask to use for exported files
  -p, --port=<port>         Port to use
      --udp                 Use UDP instead of TCP
      --portmap-disabled    Disable embedded portmap service
  -e, --exports=<exports>   Path to exports file (nsf4j advanced configuration)
  -h, --help                Display this help message
```

### Configuration File

Configuration file is loaded from *nfs4j.yml* in working directory by default.

You can set a custom filepath to this configuration file with `-c, --config=<config>` command line option.
```yaml
port: 2048
udp: false
permissions:
  gid: 1000
  uid: 1000
  mask: 0644
shares:
  - 'C:\Users\Toilal\projects\planireza'
  - 'C:\Users\Toilal\projects\docker-devbox'
  - 'D:\'
```

*Make sure you are using single quote on shares definition strings in yaml configuration file to avoid issues 
with backslashes.*

## Shares configuration

- If no share is configured, the current working directory is published under the root alias ```/```.

```bash
# Service side
java -jar nfs4j-daemon.jar
# Client side
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

- If a single share is configured, it's published under the root alias ```/```.

```bash
# Service side
java -jar nfs4j-daemon.jar C:\my\folder
# Client side
mount -t nfs 192.168.1.1:/ /mnt/nfs4j
```

- If many shares are configured, they will be aliased automatically based on their local path.

```bash
# Server side
java -jar nfs4j-daemon.jar C:\my\folder D:\another\folder
# Client side
mount -t nfs 192.168.1.1:/C/my/folder /mnt/nfs4j-1
mount -t nfs 192.168.1.1:/D/another/folder /mnt/nfs4j-2
```

- Alias can be defined manually by adding its value after the local path of the share, using 
```:``` as separator.

```
# Service side
java -jar nfs4j-daemon.jar C:\my\folder:/folder1 D:\another\folder:/folder2
# Client side
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

## Symbolic links support on Windows

On default Windows installation, unprivileged user can't create symbolic links, so nfs4j may fail to create symbolic 
links too.

You have some options to workaround this issue.

- Run `nfs4j-daemon` as Administrator.
- Tweak the Local Group Policy to allow *Create symbolic links* to the user running `nfs4j-daemon`. (See this [StackOverflow post](https://superuser.com/questions/104845/permission-to-make-symbolic-links-in-windows-7#answer-105381))
