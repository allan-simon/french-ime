# -*- mode: ruby -*-
# vi: set ft=ruby ts=2 sw=2 expandtab :

UID = Process.euid
PROJECT="french_input"

app_vars = {
  'HOST_USER_UID' => UID,
}

# to avoid typing --provider docker --no-parallel
# at every vagrant up
ENV['VAGRANT_NO_PARALLEL'] = 'yes'
ENV['VAGRANT_DEFAULT_PROVIDER'] = 'docker'

Vagrant.configure(2) do |config|

  config.vm.define "dev", primary: true do |app|

    app.vm.provider "docker" do |d|
      d.force_host_vm = false
      d.image = "allansimon/docker-devbox-php:latest"
      d.name = "#{PROJECT}_dev"
      d.volumes =  [
        "#{ENV['HOME']}/.ssh:/home/vagrant/.ssh",
      ]
      d.env = app_vars
      d.has_ssh = true
    end

    # so that we can git push from within the container
    app.vm.provision "file", source: "~/.gitconfig", destination: ".gitconfig"

    app.vm.provision :shell, privileged: false, env: app_vars, :inline => <<-END
      set -e
      echo "cd /vagrant/" >> /home/vagrant/.zshrc
    END

    app.vm.provision "print_help", type: "shell" do |s|
      s.inline = "
        echo 'done, you can now run `vagrant ssh` to connect to the service'
        chown vagrant:vagrant /home/vagrant/.zshrc
      "
    end

    app.ssh.username = "vagrant"
    app.ssh.password = ""

  end
end
